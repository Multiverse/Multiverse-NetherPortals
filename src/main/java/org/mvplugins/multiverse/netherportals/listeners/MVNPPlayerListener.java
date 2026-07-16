package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.utils.EndPlatformCreator;
import org.mvplugins.multiverse.netherportals.utils.MVLinkChecker;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.utils.customportals.CustomPortalsHandler;

@Service
final class MVNPPlayerListener implements MVNPListener {

    private final NetherPortalsConfig config;
    private final LinksManager linksManager;
    private final MVLinkChecker linkChecker;
    private final WorldManager worldManager;
    private final EndPlatformCreator endPlatformCreator;
    private final CustomPortalsHandler customPortalsHandler;

    private static final boolean HAS_RESPAWN_REASON = ReflectHelper.hasClass("org.bukkit.event.player.PlayerRespawnEvent$RespawnReason");
    private static final boolean HAS_RESPAWN_FLAG = ReflectHelper.hasClass("org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag");

    @Inject
    public MVNPPlayerListener(
            @NotNull NetherPortalsConfig config,
            @NotNull LinksManager linksManager,
            @NotNull MVLinkChecker linkChecker,
            @NotNull WorldManager worldManager,
            @NotNull EndPlatformCreator endPlatformCreator,
            @NotNull CustomPortalsHandler customPortalsHandler) {
        this.config = config;
        this.linksManager = linksManager;
        this.linkChecker = linkChecker;
        this.worldManager = worldManager;
        this.endPlatformCreator = endPlatformCreator;
        this.customPortalsHandler = customPortalsHandler;
    }

    @EventMethod
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!config.shouldHandleEndExitRespawn()) {
            return;
        }

        if (HAS_RESPAWN_REASON) {
            if (event.getRespawnReason() == PlayerRespawnEvent.RespawnReason.END_PORTAL) {
                handleEndPortalExit(event);
            }
        } else if (HAS_RESPAWN_FLAG) {
            if (event.getRespawnFlags().contains(PlayerRespawnEvent.RespawnFlag.END_PORTAL)) {
                handleEndPortalExit(event);
            }
        }
    }

    private void handleEndPortalExit(PlayerRespawnEvent event) {
        MultiverseWorld fromWorld = worldManager.getWorld(event.getPlayer().getWorld()).getOrNull();
        if (fromWorld == null) {
            Logging.fine("Ignoring end portal exit for player %s as it is not in a multiverse world!",
                    event.getPlayer().getName());
            return;
        }

        String toWorldName = linksManager.getWorldLink(fromWorld.getName(), WorldLinkType.END)
                .getOrElse(() -> linkChecker.getAutoLink(fromWorld.getName(), PortalType.ENDER));
        if (toWorldName == null) {
            Logging.fine("Player '%s' will be respawned in world '%s' at default location due to end portal exit (no linked world).",
                    event.getPlayer().getName(), fromWorld.getName());
            return;
        }

        if (fromWorld.getBedRespawn()
                && event.isBedSpawn()
                && event.getRespawnLocation().getWorld().getName().equals(toWorldName)) {
            Logging.fine("Player '%s' will be respawned in world '%s' at their bed spawn location due to end portal exit.",
                    event.getPlayer().getName(), toWorldName);
            return;
        }

        String toNetherWorld = linksManager.getWorldLink(toWorldName, WorldLinkType.NETHER).getOrNull();
        if (fromWorld.getAnchorRespawn()
                && event.isAnchorSpawn()
                && event.getRespawnLocation().getWorld().getName().equals(toNetherWorld)) {
            Logging.fine("Player '%s' will be respawned in world '%s' at their respawn anchor location due to end portal exit.",
                    event.getPlayer().getName(), toNetherWorld);
            return;
        }

        worldManager.getWorld(toWorldName).map(MultiverseWorld::getSpawnLocation)
                .peek(spawnLocation -> {
                    event.setRespawnLocation(spawnLocation);
                    Logging.fine("Player '%s' will be respawned in world '%s' at location %s due to end portal exit.",
                            event.getPlayer().getName(), toWorldName, spawnLocation);
                })
                .onEmpty(() -> Logging.warning("Ignored end portal exit for player '%s' because linked world '%s' is not a multiverse world.",
                        event.getPlayer().getName(), toWorldName));
    }

    @EventMethod
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            Logging.finest("PlayerPortalEvent was cancelled! NOT teleporting!");
            return;
        }

        Location currentLocation = event.getFrom().clone();
        if (customPortalsHandler.isHandledByCustomPortals(event.getPlayer(), currentLocation)) {
            return;
        }

        PortalType type;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            type = PortalType.ENDER;
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            type = PortalType.NETHER;
        } else {
            return;
        }

        Player player = event.getPlayer();

        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = WorldLinkType.fromPortalType(type)
                .flatMap(worldLinkType -> linksManager.getWorldLink(currentWorld, worldLinkType))
                .getOrElse(() -> linkChecker.getAutoLink(currentWorld, type));

        if (currentWorld.equalsIgnoreCase(linkedWorld) || linkedWorld == null) {
            event.setCancelled(true);
            return;
        }

        Location newTo = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, player);
        if  (newTo == null) {
            event.setCancelled(true);
            return;
        }
        event.setTo(newTo);

        LoadedMultiverseWorld fromWorld = this.worldManager.getLoadedWorld(event.getFrom().getWorld()).getOrNull();
        LoadedMultiverseWorld toWorld = this.worldManager.getLoadedWorld(event.getTo().getWorld()).getOrNull();
        if (fromWorld == null || toWorld == null) {
            Logging.fine("Player '%s' is trying to enter a portal from/to a world that is not known to Multiverse. From: %s, To: %s",
                    player.getName(), event.getFrom().getWorld().getName(), event.getTo().getWorld().getName());
            return;
        }

        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Logging.fine("Player '%s' will be teleported to the spawn of '%s' since they used an end exit portal.",
                    player.getName(), toWorld.getName());
            event.setCanCreatePortal(false);
            if (toWorld.getBedRespawn()
                    && player.getBedSpawnLocation() != null
                    && toWorld.getUID().equals(player.getBedSpawnLocation().getWorld().getUID())) {
                event.setTo(player.getBedSpawnLocation());
            } else {
                event.setTo(toWorld.getSpawnLocation());
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
            event.setCanCreatePortal(true);
        } else if (toWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
            Location spawnLocation = endPlatformCreator.getVanillaLocation(player, event.getTo().getWorld());
            event.setTo(spawnLocation);
            endPlatformCreator.createEndPlatform(spawnLocation.getWorld(), config.isEndPlatformDropBlocks());
        }
    }
}
