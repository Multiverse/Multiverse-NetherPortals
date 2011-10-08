package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.logging.Level;

public class MVNPPlayerListener extends PlayerListener {

    private MultiverseNetherPortals plugin;
    private MVNameChecker nameChecker;
    private MVWorldManager worldManager;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location currentLocation = event.getFrom().clone();
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
        MVWorld tpto = this.worldManager.getMVWorld(worldstring);
        if (tpto != null && this.plugin.getCore().getMVPerms().canEnterWorld(event.getPlayer(), tpto) && this.worldManager.isMVWorld(fromLocation.getWorld().getName())) {
            // Set the output location to the same XYZ coords but different world
            double toScaling = this.worldManager.getMVWorld(tpto.getName()).getScaling();
            double fromScaling = this.worldManager.getMVWorld(event.getFrom().getWorld().getName()).getScaling();

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
