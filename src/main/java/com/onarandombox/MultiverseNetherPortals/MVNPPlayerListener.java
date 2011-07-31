package com.onarandombox.MultiverseNetherPortals;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;

public class MVNPPlayerListener extends PlayerListener {

    private MultiverseNetherPortals plugin;
    private MVNameChecker nameChecker;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location currentLocation = event.getFrom();
        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = this.plugin.getWorldLink(currentWorld);

        if (linkedWorld != null) {
            this.getNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld));
        } else {
            this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(currentWorld));
        }
    }

    private void getNewTeleportLocation(PlayerPortalEvent event, Location fromLocation, String worldstring) {
        MVWorld tpto = this.plugin.core.getMVWorld(worldstring);
        if (tpto != null && this.plugin.core.ph.canEnterWorld(event.getPlayer(), tpto) && this.plugin.core.isMVWorld(fromLocation.getWorld().getName())) {
            // Set the output location to the same XYZ coords but different world
            // TODO: Add scaling
            double toScaling = this.plugin.core.getMVWorld(tpto.getName()).getScaling();
            double fromScaling = this.plugin.core.getMVWorld(event.getFrom().getWorld().getName()).getScaling();

            fromLocation = this.getScaledLocation(fromLocation, fromScaling, toScaling);
            fromLocation.setWorld(tpto.getCBWorld());
            event.setTo(fromLocation);
        } else {
            this.plugin.log(Level.WARNING, "Looks like " + worldstring + " does not exist. Whoops on your part!");
            this.plugin.log(Level.WARNING, "You should check your Multiverse-NetherPortals configs!!");
            // Set the event to redirect back to the same portal
            // otherwise they sit in the jelly stuff forever!
            event.setTo(fromLocation);
        }
    }

    private Location getScaledLocation(Location fromLocation, double fromScaling, double toScaling) {
        double scaling = fromScaling / toScaling;
        fromLocation.setX(fromLocation.getX() * scaling);
        fromLocation.setZ(fromLocation.getZ() * scaling);
        return fromLocation;
    }
}
