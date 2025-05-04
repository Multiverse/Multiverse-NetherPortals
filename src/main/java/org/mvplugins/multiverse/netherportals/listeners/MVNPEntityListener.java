package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.utils.EndPlatformCreator;
import org.mvplugins.multiverse.netherportals.utils.MVEventRecord;
import org.mvplugins.multiverse.netherportals.utils.MVLinkChecker;
import org.mvplugins.multiverse.netherportals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.event.MVPlayerTouchedPortalEvent;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entrycheck.WorldEntryCheckerProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
final class MVNPEntityListener implements MVNPListener {

    private static final int COOLDOWN = 250;

    private final Map<String, Date> playerErrors;

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final WorldEntryCheckerProvider entryCheckerProvider;
    private final WorldManager worldManager;
    private final LocationManipulation locationManipulation;
    private final MVEventRecord eventRecord;
    private final EndPlatformCreator endPlatformCreator;
    // the event record is used to track players that are currently standing
    // inside portals. it's used so that we don't need to run the the onEntityPortalEnter
    // listener more than once for a given player. that also means players are
    // only messaged once about why they can't go through a given portal.

    @Inject
    MVNPEntityListener(
            @NotNull MultiverseNetherPortals plugin,
            @NotNull MVNameChecker nameChecker,
            @NotNull MVLinkChecker linkChecker,
            @NotNull WorldEntryCheckerProvider entryCheckerProvider,
            @NotNull WorldManager worldManager,
            @NotNull LocationManipulation locationManipulation,
            @NotNull MVEventRecord eventRecord,
            @NotNull EndPlatformCreator endPlatformCreator) {
        this.playerErrors = new HashMap<>();
        this.plugin = plugin;
        this.nameChecker = nameChecker;
        this.linkChecker = linkChecker;
        this.entryCheckerProvider = entryCheckerProvider;
        this.worldManager = worldManager;
        this.locationManipulation = locationManipulation;
        this.eventRecord = eventRecord;
        this.endPlatformCreator = endPlatformCreator;
    }

    /**
     * Shoot a player back from a portal. Returns true iff bounceback is
     * enabled and the PortalType is supported (see below), otherwise
     * returns false.
     * <p>
     * Currently, only PortalType.NETHER is supported.
     *
     * @param p     The Player to shoot back.
     * @param block The Block the player will be shot back from.
     * @param type  The type of portal the Player is trying to enter.
     * @return      {@code true} iff the player was bounced back.
     */
    protected boolean shootPlayer(Player p, Block block, PortalType type) {
        if (!plugin.isUsingBounceBack()) {
            Logging.finest("Bounceback is disabled. The player is free to walk into the portal!");
            return false;
        }

        // add player and time to the error map
        // this prevents positive feedback loops
        this.playerErrors.put(p.getName(), new Date());

        double newVecX = 0;
        double newVecZ = 0;
        double strength = 1;
        boolean playerBounced = false;

        StringBuilder debugMessage = new StringBuilder().append("Player: ").append(p.getName());
        if (type == PortalType.ENDER) {
            debugMessage.append(" entered an End Portal. There is currently no bounceback implementation for End Portals.");
        } else if (type == PortalType.NETHER) {
            // determine portal orientation by checking if the block to the west/east is also a nether portal block
            if (block.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL || block.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
                // we add 0.5 to the location of the block to get the center
                if (p.getLocation().getZ() < block.getLocation().getZ() + 0.5) {
                    debugMessage.append(" entered Nether Portal from the North");
                    newVecZ = -1 * strength;
                } else {
                    debugMessage.append(" entered Nether Portal from the South");
                    newVecZ = 1 * strength;
                }
            } else {
                // we add 0.5 to the location of the block to get the center
                if (p.getLocation().getX() < block.getLocation().getX() + 0.5) {
                    debugMessage.append(" entered Nether Portal from the West");
                    newVecX = -1 * strength;
                } else {
                    debugMessage.append(" entered Nether Portal from the East");
                    newVecX = 1 * strength;
                }
            }

            debugMessage.append(". They will be bounced back!");
            p.teleport(p.getLocation().clone().add(newVecX, .2, newVecZ));
            p.setVelocity(new Vector(newVecX, .6, newVecZ));
            playerBounced = true;
        } else {
            debugMessage.append(" entered an Unsupported Portal Type (").append(type).append(").");
        }

        Logging.finer(debugMessage.toString());
        return playerBounced;
    }

    /**
     * Figures out the destination of a portal, given its type, and the world it resides on.
     *
     * @param e               The entity in the portal.
     * @param currentLocation The location of the portal.
     * @param type            The type of the portal. Must be a value from the PortalType enum.
     * @param currentWorld    The name of the world the portal resides on.
     * @param linkedWorld     The name of the world linked to {@code currentWorld}, if any.
     * @return
     */
    @Nullable
    private Location getLocation(Entity e, Location currentLocation, PortalType type, String currentWorld, String linkedWorld) {
        Location newTo = null;

        if (!currentWorld.equalsIgnoreCase(linkedWorld)) {
            if (linkedWorld != null) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, e);
            } else {
                String destinationWorld = "";

                if (this.nameChecker.isValidEndName(currentWorld)) {
                    if (type == PortalType.ENDER) {
                        destinationWorld = this.nameChecker.getNormalName(currentWorld, type);
                    } else if (type == PortalType.NETHER) {
                        destinationWorld = this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, type));
                    }
                } else if (this.nameChecker.isValidNetherName(currentWorld)) {
                    if (type == PortalType.ENDER) {
                        destinationWorld = this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, type));
                    } else if (type == PortalType.NETHER) {
                        destinationWorld = this.nameChecker.getNormalName(currentWorld, type);
                    }
                } else {
                    if (type == PortalType.ENDER) {
                        destinationWorld = this.nameChecker.getEndName(currentWorld);
                    } else if (type == PortalType.NETHER) {
                        destinationWorld = this.nameChecker.getNetherName(currentWorld);
                    }
                }

                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, destinationWorld, e);
            }
        }

        return newTo;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) event.getEntity();
        Location currentLocation = this.locationManipulation.getBlockLocation(event.getLocation());

        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }

        PortalType type;
        // determine what kind of portal the player is using
        if (currentLocation.getBlock().getType() == Material.END_PORTAL) {
            type = PortalType.ENDER;
        } else if (currentLocation.getBlock().getType() == Material.NETHER_PORTAL) {
            type = PortalType.NETHER;
        } else {
            return;
        }

        if (eventRecord.isInRecord(type, p.getUniqueId())) {
            // no need to carry on, the player is already in the event record
            return;
        } else {
            // we'll add the player to the event record since they're standing
            // in a portal. they'll automatically be removed when they leave
            eventRecord.addToRecord(type, p.getUniqueId());
        }

        MVPlayerTouchedPortalEvent playerTouchedPortalEvent = new MVPlayerTouchedPortalEvent(p, event.getLocation());
        this.plugin.getServer().getPluginManager().callEvent(playerTouchedPortalEvent);
        Location eventLocation = event.getLocation().clone();
        if (!playerTouchedPortalEvent.canUseThisPortal()) {
            // Someone else said the player is not allowed to go here.
            if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                eventRecord.removeFromRecord(type, p.getUniqueId());
            }

            Logging.finest("Someone requested that this player be bounced back!");
        }
        if (playerTouchedPortalEvent.isCancelled()) {
            Logging.finest("Someone cancelled the enter Event for NetherPortals!");
            return;
        }

        if (this.playerErrors.containsKey(p.getName())) {
            Date lastTry = this.playerErrors.get(p.getName());
            if (lastTry.getTime() + this.COOLDOWN > new Date().getTime()) {
                return;
            }
            this.playerErrors.remove(p.getName());
        }

        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);
        Location toLocation = getLocation(p, currentLocation, type, currentWorld, linkedWorld);

        if (toLocation == null) {
            if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                eventRecord.removeFromRecord(type, p.getUniqueId());
            }

            if (currentWorld.equalsIgnoreCase(linkedWorld)) {
                if (this.plugin.isSendingDisabledPortalMessage()) {
                    if (type == PortalType.ENDER) {
                        p.sendMessage("End Portals have been disabled in this world!");
                    } else {
                        p.sendMessage("Nether Portals have been disabled in this world!");
                    }
                }
            } else {
                if (this.plugin.isSendingNoDestinationMessage()) {
                    p.sendMessage("This portal goes nowhere!");
                    if (type == PortalType.ENDER) {
                        p.sendMessage("No specific end world has been linked to this world and '" + this.nameChecker.getEndName(currentWorld) + "' is not a world.");
                    } else {
                        p.sendMessage("No specific nether world has been linked to this world and '" + this.nameChecker.getNetherName(currentWorld) + "' is not a world.");
                    }
                }
            }

            return;
        }

        LoadedMultiverseWorld fromWorld = this.worldManager.getLoadedWorld(p.getLocation().getWorld()).getOrNull();
        LoadedMultiverseWorld toWorld = this.worldManager.getLoadedWorld(toLocation.getWorld()).getOrNull();

        if (fromWorld.getBukkitWorld().eq(toWorld.getBukkitWorld())) {
            // The player is Portaling to the same world.
            Logging.finer("Player '" + p.getName() + "' is portaling to the same world.");
            return;
        }

        entryCheckerProvider.forSender(p).canEnterWorld(fromWorld, toWorld)
                .onSuccess((result) -> {
                    Logging.fine("Player '" + p.getName() + "' was ALLOWED ACCESS to '" + toWorld.getName() + "'" + ": " + result);
                })
                .onFailure((result) -> {
                    if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                        eventRecord.removeFromRecord(type, p.getUniqueId());
                    }
                    Logging.fine("Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getName() + "'" + ": " + result);
                });
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.isCancelled()) {
            Logging.finest("EntityPortalEvent was cancelled! NOT teleporting!");
            return;
        }

        if (event.getTo() == null) {
            Logging.warning("getTo() location in EntityPortalEvent is null.");
            return;
        }

        // Don't mess with other people's stuff
        if (!plugin.isHandledByNetherPortals(event.getFrom())) {
            return;
        }

        // This is the entity event, don't teleport entities if we're not supposed to
        if (!this.plugin.isTeleportingEntities()) {
            event.setCancelled(true);
            return;
        }

        Entity entity = event.getEntity();

        Location fromLocation = event.getFrom();
        Location originalToLocation = event.getTo();

        World fromWorld = fromLocation.getWorld();
        World originalToWorld = originalToLocation.getWorld();

        if (fromWorld == null || originalToWorld == null) {
            Logging.warning("from/to world is null in EntityPortalEvent for %s", entity.getName());
            return;
        }

        PortalType type;
        if (originalToWorld.getEnvironment() == World.Environment.NETHER
                || (fromWorld.getEnvironment() == World.Environment.NETHER && originalToWorld.getEnvironment() == World.Environment.NORMAL)) {
            type = PortalType.NETHER;
        } else if (originalToWorld.getEnvironment() == World.Environment.THE_END
                || (fromWorld.getEnvironment() == World.Environment.THE_END && originalToWorld.getEnvironment() == World.Environment.NORMAL)) {
            type = PortalType.ENDER;
        } else {
            return;
        }

        String fromWorldName = fromWorld.getName();
        String linkedWorldName = this.plugin.getWorldLink(fromWorldName, type);
        Location newToLocation = getLocation(entity, fromLocation, type, fromWorldName, linkedWorldName); // Gets the player spawn location from the portal spawn location

        // If we can't get a valid location, cancel the event
        if (newToLocation == null) {
            event.setCancelled(true);
            return;
        }

        event.setTo(newToLocation);
        LoadedMultiverseWorld newToWorld = this.worldManager.getLoadedWorld(newToLocation.getWorld()).getOrNull();

        // If we are going to the overworld from the end
        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            event.setTo(newToWorld.getSpawnLocation());
        }
        // If we are going to the overworld from the nether
//        else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
//            try {
//                Class.forName("org.bukkit.TravelAgent");
//                event.getPortalTravelAgent().setCanCreatePortal(true);
//                event.setTo(event.getPortalTravelAgent().findOrCreate(newToLocation));
//            } catch (ClassNotFoundException ignore) {
//                Logging.fine("TravelAgent not available for EntityPortalEvent for " + entity.getName() + ". Their destination may not be correct.");
//                event.setTo(newToLocation);
//            }
//        }
        // If we are going to the end from anywhere
        else if (newToWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Location spawnLocation = endPlatformCreator.getVanillaLocation(entity, newToWorld);
            event.setTo(spawnLocation);
            endPlatformCreator.createEndPlatform(spawnLocation.getWorld(), plugin.isEndPlatformDropBlocks());
        }
    }

    @EventHandler
    public void onEntityPortalExit(EntityPortalExitEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            eventRecord.removeFromRecord(PortalType.ENDER, player.getUniqueId());
            eventRecord.removeFromRecord(PortalType.NETHER, player.getUniqueId());
        }
    }
}
