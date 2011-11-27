package com.onarandombox.MultiverseNetherPortals.listeners;

import com.fernferret.allpay.GenericBank;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.logging.Level;

public class MVNPPlayerListener extends PlayerListener {

    private MultiverseNetherPortals plugin;
    private MVNameChecker nameChecker;
    private MVWorldManager worldManager;
    private PermissionTools pt;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location currentLocation = event.getFrom().clone();
        String currentWorld = currentLocation.getWorld().getName();
        String type = "end";
        if (event.getFrom().getBlock().getType() == Material.PORTAL) {
            System.out.println("Normal!");
            type = "nether";
        }
        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);

        if (linkedWorld != null) {
            this.getNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld));
        } else {
            this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(currentWorld));
        }

        if (event.getTo() == null || event.getFrom() == null) {
            return;
        }
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            // The player is Portaling to the same world.
            this.plugin.log(Level.FINER, "Player '" + event.getPlayer().getName() + "' is portaling to the same world.");
            return;
        }
        event.setCancelled(!pt.playerHasMoneyToEnter(fromWorld, toWorld, event.getPlayer(), event.getPlayer()));
        if (event.isCancelled()) {
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + event.getTo().getWorld().getName() +
                    "' because they don't have the FUNDS required to enter.");
            return;
        }
        if (MultiverseCore.EnforceAccess) {
            event.setCancelled(!pt.playerCanGoFromTo(fromWorld, toWorld, event.getPlayer(), event.getPlayer()));
            if (event.isCancelled()) {
                this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + event.getTo().getWorld().getName() +
                        "' because they don't have: multiverse.access." + event.getTo().getWorld().getName());
            }
        } else {
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was allowed to go to '" + event.getTo().getWorld().getName() + "' because enforceaccess is off.");
        }
    }

    private void getNewTeleportLocation(PlayerPortalEvent event, Location fromLocation, String worldstring) {
        MultiverseWorld tpto = this.worldManager.getMVWorld(worldstring);
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
