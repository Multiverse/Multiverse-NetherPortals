package org.mvplugins.multiverse.netherportals.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entrycheck.WorldEntryCheckerProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

@Service
public class MVLinkChecker {
    private final WorldManager worldManager;
    private final WorldEntryCheckerProvider entryCheckerProvider;

    @Inject
    MVLinkChecker(WorldManager worldManager, WorldEntryCheckerProvider entryCheckerProvider) {
        this.worldManager = worldManager;
        this.entryCheckerProvider = entryCheckerProvider;
    }

    public Location findNewTeleportLocation(Location fromLocation, String worldString, Entity e) {
        LoadedMultiverseWorld tpFrom = this.worldManager.getLoadedWorld(fromLocation.getWorld()).getOrNull();
        LoadedMultiverseWorld tpTo = this.worldManager.getLoadedWorld(worldString).getOrNull();

        if (tpTo == null) {
            Logging.fine("Can't find world " + worldString);
        } else if (e instanceof Player && !this.entryCheckerProvider.forSender(e).canEnterWorld(tpFrom, tpTo).isSuccess()) {
            Logging.warning("Player " + e.getName() + " can't enter world " + worldString);
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
