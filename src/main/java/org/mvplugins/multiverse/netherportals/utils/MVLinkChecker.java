package org.mvplugins.multiverse.netherportals.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entrycheck.WorldEntryCheckerProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;

@Service
public class MVLinkChecker {
    private final WorldManager worldManager;
    private final MVNameChecker nameChecker;

    @Inject
    MVLinkChecker(WorldManager worldManager, MVNameChecker nameChecker) {
        this.worldManager = worldManager;
        this.nameChecker = nameChecker;
    }

    /**
     * Gets the auto-link for a given world and portal type.
     *
     * @param fromWorld     The world name to get the auto-link for.
     * @param portalType    The type of portal to get the auto-link for.
     * @return The auto-link for the given world and portal type if it exists, otherwise null.
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @Nullable String getAutoLink(String fromWorld, PortalType portalType) {
        if (nameChecker.isValidEndName(fromWorld)) {
            if (portalType == PortalType.ENDER) {
                return nameChecker.getNormalName(fromWorld, portalType);
            } else if (portalType == PortalType.NETHER) {
                return nameChecker.getNetherName(this.nameChecker.getNormalName(fromWorld, portalType));
            }
        } else if (this.nameChecker.isValidNetherName(fromWorld)) {
            if (portalType == PortalType.ENDER) {
                return nameChecker.getEndName(this.nameChecker.getNormalName(fromWorld, portalType));
            } else if (portalType == PortalType.NETHER) {
                return nameChecker.getNormalName(fromWorld, portalType);
            }
        } else {
            if (portalType == PortalType.ENDER) {
                return nameChecker.getEndName(fromWorld);
            } else if (portalType == PortalType.NETHER) {
                return nameChecker.getNetherName(fromWorld);
            }
        }
        return null;
    }

    public Location findNewTeleportLocation(Location fromLocation, String worldString, Entity e) {
        LoadedMultiverseWorld tpFrom = this.worldManager.getLoadedWorld(fromLocation.getWorld()).getOrNull();
        LoadedMultiverseWorld tpTo = this.worldManager.getLoadedWorld(worldString).getOrNull();

        if (tpTo == null) {
            Logging.fine("Can't find world " + worldString);
        } else if (!this.worldManager.isLoadedWorld(fromLocation.getWorld())) {
            Logging.warning("World " + fromLocation.getWorld().getName() + " is not a Multiverse world");
        } else {
            String entityType = (e instanceof Player) ? " player " : " entity ";
            Logging.fine("Finding new teleport location for" + entityType + e.getName() + " to world " + worldString);

            // Set the output location to the same XYZ coords but different world
            double fromScaling = tpFrom.getScale();
            double toScaling = tpTo.getScale();

            this.scaleLocation(fromLocation, fromScaling / toScaling);
            fromLocation.setWorld(tpTo.getBukkitWorld().getOrNull());
            return fromLocation;
        }

        return null;
    }

    private void scaleLocation(Location fromLocation, double scaling) {
        fromLocation.setX(fromLocation.getX() * scaling);
        fromLocation.setZ(fromLocation.getZ() * scaling);
    }
}
