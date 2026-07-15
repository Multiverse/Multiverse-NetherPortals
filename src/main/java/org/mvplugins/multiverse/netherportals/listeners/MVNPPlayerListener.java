package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.utils.EndPlatformCreator;
import org.mvplugins.multiverse.netherportals.utils.MVLinkChecker;
import org.mvplugins.multiverse.netherportals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

@Service
final class MVNPPlayerListener implements MVNPListener {

    private final MultiverseNetherPortals plugin;
    private final NetherPortalsConfig config;
    private final LinksManager linksManager;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final WorldManager worldManager;
    private final EndPlatformCreator endPlatformCreator;

    private final Advancement enterNetherAdvancement;
    private final Advancement enterEndAdvancement;

    private static final String ENTER_NETHER_CRITERIA = "entered_nether";
    private static final String ENTER_END_CRITERIA = "entered_end";

    private static final boolean HAS_RESPAWN_REASON = ReflectHelper.hasClass("org.bukkit.event.player.PlayerRespawnEvent$RespawnReason");
    private static final boolean HAS_RESPAWN_FLAG = ReflectHelper.hasClass("org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag");

    @Inject
    public MVNPPlayerListener(
            @NotNull MultiverseNetherPortals plugin,
            @NotNull NetherPortalsConfig config,
            @NotNull LinksManager linksManager,
            @NotNull MVNameChecker nameChecker,
            @NotNull MVLinkChecker linkChecker,
            @NotNull WorldManager worldManager,
            @NotNull EndPlatformCreator endPlatformCreator) {
        this.plugin = plugin;
        this.config = config;
        this.linksManager = linksManager;
        this.nameChecker = nameChecker;
        this.linkChecker = linkChecker;
        this.worldManager = worldManager;
        this.endPlatformCreator = endPlatformCreator;

        this.enterNetherAdvancement = tryGetAdvancement("story/enter_the_nether");
        this.enterEndAdvancement = tryGetAdvancement("story/enter_the_end");
    }

    private Advancement tryGetAdvancement(String advancementName) {
        return Try.of(() -> this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(advancementName)))
                .recover(e -> null)
                .getOrNull();
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
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
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

        Location newTo;
        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = WorldLinkType.fromPortalType(type)
                .flatMap(worldLinkType -> linksManager.getWorldLink(currentWorld, worldLinkType))
                .getOrNull();
        if (currentWorld.equalsIgnoreCase(linkedWorld)) {
            newTo = null;
        } else if (linkedWorld != null) {
            newTo = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, player);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER), player);
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)), player);
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.ENDER)), player);
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.ENDER), player);
            }
        } else {
            if (type == PortalType.ENDER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(currentWorld), player);
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(currentWorld), player);
            }
        }

        if (newTo != null) {
            event.setTo(newTo);
        } else {
            event.setCancelled(true);
            return;
        }

        LoadedMultiverseWorld fromWorld = this.worldManager.getLoadedWorld(event.getFrom().getWorld()).getOrNull();
        LoadedMultiverseWorld toWorld = this.worldManager.getLoadedWorld(event.getTo().getWorld()).getOrNull();

        if (!event.isCancelled()) {
            if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
                Logging.fine("Player '" + player.getName() + "' will be teleported to the spawn of '" + toWorld.getName() + "' since they used an end exit portal.");
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

            // Advancements need to be triggered manually
            if (type == PortalType.NETHER && event.getTo().getWorld().getEnvironment() == World.Environment.NETHER && enterNetherAdvancement != null) {
                awardAdvancement(player, enterNetherAdvancement, ENTER_NETHER_CRITERIA);
            } else if (type == PortalType.ENDER && event.getTo().getWorld().getEnvironment() == World.Environment.THE_END && enterEndAdvancement != null) {
                awardAdvancement(player, enterEndAdvancement, ENTER_END_CRITERIA);
            }
        }
    }

    /**
     * Award an advancement criteria to a player if not already awarded.
     *
     * @param player        Target player to award the advancement criteria to.
     * @param advancement   {@link Advancement} the criteria belongs to.
     * @param criteria      Criteria to award the player.
     */
    private void awardAdvancement(Player player, Advancement advancement, String criteria) {
        if (advancement == null) {
            Logging.fine("No advancement found for target criteria: %s", criteria);
            return;
        }
        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
        if (advancementProgress.isDone()) {
            Logging.fine("%s has already been awarded advancement criteria %s.", player.getName(), criteria);
            return;
        }
        if (!advancementProgress.awardCriteria(criteria)) {
            Logging.warning("Unable to award advancement criteria %s to %s.", criteria, player.getName());
            return;
        }
        Logging.fine("Awarded advancement criteria %s to %s.", criteria, player.getName());
    }
}
