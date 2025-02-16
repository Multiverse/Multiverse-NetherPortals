package com.onarandombox.MultiverseNetherPortals.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
            double fromScaling = this.worldManager.getMVWorld(fromLocation.getWorld().getName()).getScaling();
            double toScaling = tpTo.getScaling();

            // Clone new location to avoid affecting the original location
            Location newTargetLocation = fromLocation.clone();
            this.scaleLocation(newTargetLocation, fromScaling / toScaling);
            newTargetLocation.setWorld(tpTo.getCBWorld());
            return newTargetLocation;
        }

        return null;
    }

    private void scaleLocation(Location newTargetLocation, double scaling) {
        newTargetLocation.setX(newTargetLocation.getX() * scaling);
        newTargetLocation.setZ(newTargetLocation.getZ() * scaling);
    }
}
