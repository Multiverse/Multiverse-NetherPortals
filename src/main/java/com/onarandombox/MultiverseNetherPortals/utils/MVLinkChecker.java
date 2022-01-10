package com.onarandombox.MultiverseNetherPortals.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class MVLinkChecker {
    private final MultiverseNetherPortals plugin;
    private final MVWorldManager worldManager;

    public MVLinkChecker(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    public Location findNewTeleportLocation(Location fromLocation, String worldString, Entity e) {
        MultiverseWorld tpTo = this.worldManager.getMVWorld(worldString);

        if (tpTo == null) {
            Logging.fine("Can't find world " + worldString);
        } else if (e instanceof Player && !this.plugin.getCore().getMVPerms().canEnterWorld((Player) e, tpTo)) {
            Logging.warning("Player " + e.getName() + " can't enter world " + worldString);
        } else if (!this.worldManager.isMVWorld(fromLocation.getWorld().getName())) {
            Logging.warning("World " + fromLocation.getWorld().getName() + " is not a Multiverse world");
        } else {
            String entityType = (e instanceof Player) ? " player " : " entity ";
            Logging.fine("Finding new teleport location for" + entityType + e.getName() + " to world " + worldString);

            // Set the output location to the same XYZ coords but different world
            MultiverseWorld tpFrom = this.worldManager.getMVWorld(fromLocation.getWorld().getName());

            double fromScaling = tpFrom.getScaling();
            double toScaling = this.worldManager.getMVWorld(tpTo.getName()).getScaling();
            double yScaling = 1d * tpFrom.getCBWorld().getMaxHeight() / tpTo.getCBWorld().getMaxHeight();

            this.scaleLocation(fromLocation, fromScaling / toScaling, yScaling);
            fromLocation.setWorld(tpTo.getCBWorld());
            return fromLocation;
        }

        return null;
    }

    private void scaleLocation(Location fromLocation, double scaling, double yScaling) {
        fromLocation.setX(fromLocation.getX() * scaling);
        fromLocation.setZ(fromLocation.getZ() * scaling);
        fromLocation.setY(fromLocation.getY() * yScaling);
    }
}
