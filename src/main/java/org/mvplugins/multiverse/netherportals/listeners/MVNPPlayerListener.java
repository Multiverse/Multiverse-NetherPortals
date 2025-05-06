package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
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
import org.bukkit.event.EventHandler;
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
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final WorldManager worldManager;
    private final EndPlatformCreator endPlatformCreator;

    private final Advancement enterNetherAdvancement;
    private final Advancement enterEndAdvancement;

    private static final String ENTER_NETHER_CRITERIA = "entered_nether";
    private static final String ENTER_END_CRITERIA = "entered_end";

    @Inject
    public MVNPPlayerListener(
            @NotNull MultiverseNetherPortals plugin,
            @NotNull MVNameChecker nameChecker,
            @NotNull MVLinkChecker linkChecker,
            @NotNull WorldManager worldManager,
            @NotNull EndPlatformCreator endPlatformCreator) {
        this.plugin = plugin;
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
        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);
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
                endPlatformCreator.createEndPlatform(spawnLocation.getWorld(), plugin.isEndPlatformDropBlocks());
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
