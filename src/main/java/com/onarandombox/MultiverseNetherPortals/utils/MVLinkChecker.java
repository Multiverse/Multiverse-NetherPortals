package com.onarandombox.MultiverseNetherPortals.utils;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class MVLinkChecker {
    private MultiverseNetherPortals plugin;
    private MVWorldManager worldManager;

    public MVLinkChecker(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    public Location findNewTeleportLocation(Location fromLocation, String worldstring, Player p) {
        MultiverseWorld tpto = this.worldManager.getMVWorld(worldstring);

        if (tpto == null) {
            this.plugin.log(Level.FINE, "Can't find world " + worldstring);
        } else if (!this.plugin.getCore().getMVPerms().canEnterWorld(p, tpto)) {
            this.plugin.log(Level.WARNING, "Player " + p.getName() + " can't enter world " + worldstring);
        } else if (!this.worldManager.isMVWorld(fromLocation.getWorld().getName())) {
            this.plugin.log(Level.WARNING, "World " + fromLocation.getWorld().getName() + " is not a Multiverse world");
        } else {
            this.plugin.log(Level.FINE, "Finding new teleport location for player " + p.getName() + " to world " + worldstring);

            // Set the output location to the same XYZ coords but different world
            double toScaling = this.worldManager.getMVWorld(tpto.getName()).getScaling();
            MultiverseWorld tpfrom = this.worldManager.getMVWorld(fromLocation.getWorld().getName());
            double fromScaling = tpfrom.getScaling();
            double yScaling = 1d * tpfrom.getCBWorld().getMaxHeight() / tpto.getCBWorld().getMaxHeight();
            fromLocation = this.getScaledLocation(fromLocation, fromScaling, toScaling, yScaling);
            fromLocation.setWorld(tpto.getCBWorld());
            return fromLocation;
        }
        return null;
    }

    private Location getScaledLocation(Location fromLocation, double fromScaling, double toScaling, double yScaling) {
        double scaling = fromScaling / toScaling;
        fromLocation.setX(fromLocation.getX() * scaling);
        fromLocation.setZ(fromLocation.getZ() * scaling);
        fromLocation.setY(fromLocation.getY() * yScaling);
        return fromLocation;
    }
}
