package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.event.MVPlayerTouchedPortalEvent;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entrycheck.WorldEntryCheckerProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.utils.customportals.CustomPortalsHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace.WORLD;
import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class MVNPEntityListener implements MVNPListener {

    private static final int COOLDOWN = 250;

    private final Map<String, Date> playerErrors;

    private final NetherPortalsConfig config;
    private final LinksManager linksManager;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final WorldEntryCheckerProvider entryCheckerProvider;
    private final WorldManager worldManager;
    private final MVCommandManager commandManager;
    private final LocationManipulation locationManipulation;
    private final EndPlatformCreator endPlatformCreator;

    // the event record is used to track players that are currently standing
    // inside portals. it's used so that we don't need to run the onEntityPortalEnter
    // listener more than once for a given player. that also means players are
    // only messaged once about why they can't go through a given portal.
    private final MVEventRecord eventRecord;
    private final CustomPortalsHandler customPortalsHandler;

    @Inject
    MVNPEntityListener(
            @NotNull NetherPortalsConfig config,
            @NotNull LinksManager linksManager,
            @NotNull MVNameChecker nameChecker,
            @NotNull MVLinkChecker linkChecker,
            @NotNull WorldEntryCheckerProvider entryCheckerProvider,
            @NotNull WorldManager worldManager,
            @NotNull MVCommandManager commandManager,
            @NotNull LocationManipulation locationManipulation,
            @NotNull MVEventRecord eventRecord,
            @NotNull EndPlatformCreator endPlatformCreator,
            @NotNull CustomPortalsHandler customPortalsHandler) {
        this.playerErrors = new HashMap<>();
        this.config = config;
        this.linksManager = linksManager;
        this.nameChecker = nameChecker;
        this.linkChecker = linkChecker;
        this.entryCheckerProvider = entryCheckerProvider;
        this.worldManager = worldManager;
        this.commandManager = commandManager;
        this.locationManipulation = locationManipulation;
        this.eventRecord = eventRecord;
        this.endPlatformCreator = endPlatformCreator;
        this.customPortalsHandler = customPortalsHandler;
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
    private boolean shootPlayer(Player p, Block block, PortalType type) {
        if (!config.isUsingBounceBack()) {
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
     * @return The location of the destination world, or null if it cannot be determined.
     */
    @Nullable
    private Location getLocation(Entity e, Location currentLocation, PortalType type, String currentWorld, String linkedWorld) {
        if (currentWorld.equalsIgnoreCase(linkedWorld)) {
            return null;
        }
        if (linkedWorld != null) {
            return this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, e);
        }
        String destinationWorld = linkChecker.getAutoLink(currentWorld, type);
        return this.linkChecker.findNewTeleportLocation(currentLocation, destinationWorld, e);
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.MONITOR)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Location currentLocation = this.locationManipulation.getBlockLocation(event.getLocation());

        // determine what kind of portal the player is using
        PortalType type = switch (currentLocation.getBlock().getType()) {
            case END_PORTAL -> PortalType.ENDER;
            case NETHER_PORTAL -> PortalType.NETHER;
            default -> null;
        };
        if (type == null) {
            return;
        }

        if (eventRecord.isInRecord(type, player.getUniqueId())) {
            // no need to carry on, the player is already in the event record
            return;
        }
        // we'll add the player to the event record since they're standing
        // in a portal. they'll automatically be removed when they leave
        eventRecord.addToRecord(type, player.getUniqueId());

        if (customPortalsHandler.isHandledByCustomPortals(player, event.getLocation().clone())) {
            return;
        }

        MVPlayerTouchedPortalEvent playerTouchedPortalEvent = new MVPlayerTouchedPortalEvent(player, event.getLocation());
        Bukkit.getPluginManager().callEvent(playerTouchedPortalEvent);
        Location eventLocation = event.getLocation().clone();
        if (playerTouchedPortalEvent.isCancelled()) {
            Logging.finest("Another plugin cancelled the enter Event for NetherPortals!");
            return;
        }
        if (!playerTouchedPortalEvent.canUseThisPortal()) {
            // Someone else said the player is not allowed to go here.
            if (this.shootPlayer(player, eventLocation.getBlock(), type)) {
                eventRecord.removeFromRecord(type, player.getUniqueId());
            }

            Logging.finest("Someone requested that this player be bounced back!");
        }

        if (this.playerErrors.containsKey(player.getName())) {
            Date lastTry = this.playerErrors.get(player.getName());
            if (lastTry.getTime() + COOLDOWN > new Date().getTime()) {
                return;
            }
            this.playerErrors.remove(player.getName());
        }

        String currentWorld = currentLocation.getWorld().getName();
        WorldLinkType worldLinkType = WorldLinkType.fromPortalType(type).getOrNull();
        if (worldLinkType == null) {
            Logging.fine("Player '" + player.getName() + "' is trying to enter a portal of type '" + type + "' which is not supported by Multiverse-NetherPortals.");
            return;
        }

        String linkedWorld = linksManager.getWorldLink(currentWorld, worldLinkType).getOrNull();
        Location toLocation = getLocation(player, currentLocation, type, currentWorld, linkedWorld);

        if (toLocation == null) {
            if (this.shootPlayer(player, eventLocation.getBlock(), type)) {
                eventRecord.removeFromRecord(type, player.getUniqueId());
            }

            if (currentWorld.equalsIgnoreCase(linkedWorld)) {
                if (this.config.isSendingDisabledPortalMessage()) {
                    commandManager.getCommandIssuer(player).sendError(MVNPi18n.PORTAL_DISABLED,
                            replace("{linkType}").with(worldLinkType));
                }
            } else if (this.config.isSendingNoDestinationMessage()) {
                commandManager.getCommandIssuer(player).sendError(MVNPi18n.PORTAL_NODESTINATION);
                String autoLinkedWorld = type == PortalType.ENDER
                        ? this.nameChecker.getEndName(currentWorld)
                        : this.nameChecker.getNetherName(currentWorld);
                commandManager.getCommandIssuer(player).sendError(MVNPi18n.PORTAL_AUTOLINKEDWORLD_NOTFOUND,
                        replace("{linkType}").with(worldLinkType),
                        WORLD.with(autoLinkedWorld));
            }

            return;
        }

        LoadedMultiverseWorld fromWorld = this.worldManager.getLoadedWorld(player.getWorld()).getOrNull();
        LoadedMultiverseWorld toWorld = this.worldManager.getLoadedWorld(toLocation.getWorld()).getOrNull();
        if (fromWorld == null || toWorld == null) {
            Logging.fine("Player '%s' is trying to enter a portal from/to a world that is not known to Multiverse. From: %s, To: %s",
                    player.getName(), player.getWorld().getName(), toLocation.getWorld().getName());
            return;
        }

        if (fromWorld.getBukkitWorld().eq(toWorld.getBukkitWorld())) {
            // The player is Portaling to the same world.
            Logging.finer("Player '%s' is portaling to the same world.", player.getName());
            return;
        }

        entryCheckerProvider.forSender(player).canEnterWorld(fromWorld, toWorld)
                .onSuccess((result) ->
                        Logging.fine("Player '%s' was ALLOWED ACCESS to '%s': %s",
                                player.getName(), toWorld.getName(), result))
                .onFailure((result) -> {
                    if (this.shootPlayer(player, eventLocation.getBlock(), type)) {
                        eventRecord.removeFromRecord(type, player.getUniqueId());
                    }
                    Logging.fine("Player '%s' was DENIED ACCESS to '%s': %s",
                            player.getName(), toWorld.getName(), result);
                });
    }

    @EventMethod
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
        if (customPortalsHandler.isHandledByCustomPortals(event.getEntity(), event.getFrom())) {
            return;
        }

        // This is the entity event, don't teleport entities if we're not supposed to
        if (!this.config.isTeleportingEntities()) {
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
        String linkedWorldName = WorldLinkType.fromPortalType(type)
                .flatMap(worldLinkType -> linksManager.getWorldLink(fromWorldName, worldLinkType))
                .getOrNull();
        Location newToLocation = getLocation(entity, fromLocation, type, fromWorldName, linkedWorldName); // Gets the player spawn location from the portal spawn location

        // If we can't get a valid location, cancel the event
        if (newToLocation == null) {
            event.setCancelled(true);
            return;
        }

        event.setTo(newToLocation);
        LoadedMultiverseWorld newToWorld = this.worldManager.getLoadedWorld(newToLocation.getWorld()).getOrNull();
        if (newToWorld == null) {
            Logging.fine("Player '%s' is trying to enter a portal to a world that is not known to Multiverse. To: %s",
                    entity.getName(), newToLocation.getWorld().getName());
            event.setCancelled(true);
            return;
        }

        // If we are going to the overworld from the end
        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            event.setTo(newToWorld.getSpawnLocation());
            return;
        }

        // If we are going to the end from anywhere
        if (newToWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Location spawnLocation = endPlatformCreator.getVanillaLocation(entity, newToWorld);
            event.setTo(spawnLocation);
            endPlatformCreator.createEndPlatform(spawnLocation.getWorld(), config.isEndPlatformDropBlocks());
        }
    }

    @EventMethod
    public void onEntityPortalExit(EntityPortalExitEvent event) {
        if (event.getEntity() instanceof Player player) {
            eventRecord.removeFromRecord(PortalType.ENDER, player.getUniqueId());
            eventRecord.removeFromRecord(PortalType.NETHER, player.getUniqueId());
        }
    }
}
