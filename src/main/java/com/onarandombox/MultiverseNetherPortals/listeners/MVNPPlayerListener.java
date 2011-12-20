package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.logging.Level;

public class MVNPPlayerListener extends PlayerListener {

    private MultiverseNetherPortals plugin;
    private MVNameChecker nameChecker;
    private MVLinkChecker linkChecker;
    private MVWorldManager worldManager;
    private PermissionTools pt;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.linkChecker = new MVLinkChecker(this.plugin);
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location currentLocation = event.getFrom().clone();
        String currentWorld = currentLocation.getWorld().getName();

        PortalType type = PortalType.END;
        System.out.println(event.getFrom() + " => " + event.getFrom().getBlock().getType());
        if (event.getFrom().getBlock().getType() == Material.PORTAL) {
            type = PortalType.NETHER;
        }

        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);

        if (linkedWorld != null) {
            this.linkChecker.getNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld));
        } else {
            if(type == PortalType.END) {
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getEndName(currentWorld));
            } else {
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(currentWorld));
            }
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
        event.setCancelled(!pt.playerHasMoneyToEnter(fromWorld, toWorld, event.getPlayer(), event.getPlayer(), true));
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

}
