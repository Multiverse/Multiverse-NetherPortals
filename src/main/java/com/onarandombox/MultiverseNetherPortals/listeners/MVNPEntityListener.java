package com.onarandombox.MultiverseNetherPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.utils.EndPlatformCreator;
import com.onarandombox.MultiverseNetherPortals.utils.MVEventRecord;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MVNPEntityListener implements Listener {

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final MVWorldManager worldManager;
    private final PermissionTools pt;
    private final int cooldown = 250;
    private final MultiverseMessaging messaging;
    private final Map<String, Date> playerErrors;
    private final LocationManipulation locationManipulation;
    private final MVEventRecord eventRecord;
    // the event record is used to track players that are currently standing
    // inside portals. it's used so that we don't need to run the the onEntityPortalEnter
    // listener more than once for a given player. that also means players are
    // only messaged once about why they can't go through a given portal.

    public MVNPEntityListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = this.plugin.getNameChecker();
        this.linkChecker = this.plugin.getLinkChecker();
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.playerErrors = new HashMap<String, Date>();
        this.messaging = this.plugin.getCore().getMessaging();
        this.locationManipulation = this.plugin.getCore().getLocationManipulation();
        this.eventRecord = new MVEventRecord(this.plugin);
    }

    /**
     * Shoot a player back from a portal. Returns true iff bounceback is
     * enabled and the PortalType is supported (see below), otherwise
     * returns false.
     *
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

                boolean shouldAppearAtSpawn = false;

                if (this.nameChecker.isValidEndName(currentWorld)) {
                    if (type == PortalType.ENDER) {
                        destinationWorld = this.nameChecker.getNormalName(currentWorld, type);
                        shouldAppearAtSpawn = true;
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

                if (shouldAppearAtSpawn) {
                    MultiverseWorld tpTo = this.worldManager.getMVWorld(destinationWorld);
                    SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
                    Location safeSpawn = teleporter.getSafeLocation(tpTo.getSpawnLocation());
                    newTo = this.linkChecker.findNewTeleportLocation(safeSpawn, destinationWorld, e);
                } else {
                    newTo = this.linkChecker.findNewTeleportLocation(currentLocation, destinationWorld, e);
                }
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
            if (lastTry.getTime() + this.cooldown > new Date().getTime()) {
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
                        this.messaging.sendMessage(p, "End Portals have been disabled in this world!", false);
                    } else {
                        this.messaging.sendMessage(p, "Nether Portals have been disabled in this world!", false);
                    }
                }
            } else {
                if (this.plugin.isSendingNoDestinationMessage()) {
                    this.messaging.sendMessage(p, "This portal goes nowhere!", false);
                    if (type == PortalType.ENDER) {
                        this.messaging.sendMessage(p, "No specific end world has been linked to this world and '" + this.nameChecker.getEndName(currentWorld) + "' is not a world.", true);
                    } else {
                        this.messaging.sendMessage(p, "No specific nether world has been linked to this world and '" + this.nameChecker.getNetherName(currentWorld) + "' is not a world.", true);
                    }
                }
            }

            return;
        }

        MultiverseWorld fromWorld = this.worldManager.getMVWorld(p.getLocation().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(toLocation.getWorld().getName());

        if (fromWorld.getCBWorld().equals(toWorld.getCBWorld())) {
            // The player is Portaling to the same world.
            Logging.finer("Player '" + p.getName() + "' is portaling to the same world.");
            return;
        }
        if (!pt.playerHasMoneyToEnter(fromWorld, toWorld, p, p, false)) {
            if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                eventRecord.removeFromRecord(type, p.getUniqueId());
            }

            Logging.fine("Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                    "' because they don't have the FUNDS required to enter.");
            return;
        }

        if (this.plugin.getCore().getMVConfig().getEnforceAccess()) {
            if (!pt.playerCanGoFromTo(fromWorld, toWorld, p, p)) {
                if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                    eventRecord.removeFromRecord(type, p.getUniqueId());
                }

                Logging.fine("Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                        "' because they don't have: multiverse.access." + toWorld.getCBWorld().getName());
            }
        } else {
            Logging.fine("Player '" + p.getName() + "' was allowed to go to '" + toWorld.getCBWorld().getName() + "' because enforceaccess is off.");
        }
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

        // Are we allowed to use the nether portal travel agent?
        if (type == PortalType.NETHER) {
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.useTravelAgent(true);
            } catch (ClassNotFoundException ignore) {
                Logging.fine("TravelAgent not available for EntityPortalEvent for " + entity.getName());
            }
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
        MultiverseWorld newToWorld = this.worldManager.getMVWorld(newToLocation.getWorld());

        // If we are going to the overworld from the end
        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Logging.fine("Entity '" + entity.getName() + "' will be teleported to the spawn of '" + newToWorld.getName() + "' since they used an end exit portal.");
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.getPortalTravelAgent().setCanCreatePortal(false);
            } catch (ClassNotFoundException ignore) {
                Logging.fine("TravelAgent not available for EntityPortalEvent for " + entity.getName() + ". There may be a portal created at spawn.");
            }
            event.setTo(newToWorld.getSpawnLocation());
        }
        // If we are going to the overworld from the nether
        else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.getPortalTravelAgent().setCanCreatePortal(true);
                event.setTo(event.getPortalTravelAgent().findOrCreate(newToLocation));
            } catch (ClassNotFoundException ignore) {
                Logging.fine("TravelAgent not available for EntityPortalEvent for " + entity.getName() + ". Their destination may not be correct.");
                event.setTo(newToLocation);
            }
        }
        // If we are going to the end from anywhere
        else if (newToWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Location spawnLocation = EndPlatformCreator.getVanillaLocation(entity, newToWorld);
            event.setTo(spawnLocation);
            EndPlatformCreator.createEndPlatform(spawnLocation.getWorld(), plugin.isEndPlatformDropBlocks());
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
