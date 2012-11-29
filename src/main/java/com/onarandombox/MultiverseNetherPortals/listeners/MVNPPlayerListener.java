package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.logging.Level;

public class MVNPPlayerListener implements Listener {

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            this.plugin.log(Level.FINEST, "PlayerPortalEvent was cancelled! NOT teleporting!");
            return;
        }
        Location originalTo = event.getTo();
        if (originalTo != null) {
            originalTo = originalTo.clone();
        }
        Location currentLocation = event.getFrom().clone();
        String currentWorld = currentLocation.getWorld().getName();

        PortalType type = PortalType.END;
        if (event.getFrom().getBlock().getType() == Material.PORTAL) {
            type = PortalType.NETHER;
        }

        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);

        if (linkedWorld != null) {
            this.linkChecker.getNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                this.plugin.log(Level.FINER, "");
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER));
            } else {
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)));
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.END)));
            } else {
                this.linkChecker.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.END));
            }
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
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            // The player is Portaling to the same world.
            this.plugin.log(Level.FINER, "Player '" + event.getPlayer().getName() + "' is portaling to the same world.  Ignoring.");
            event.setTo(originalTo);
            return;
        }
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (!event.isCancelled() && fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' will be teleported to the spawn of '" + toWorld.getName() + "' since they used an end exit portal.");
            event.getPortalTravelAgent().setCanCreatePortal(false);
            if (toWorld.getBedRespawn()
                    && event.getPlayer().getBedSpawnLocation() != null
                    && event.getPlayer().getBedSpawnLocation().getWorld().getUID() == toWorld.getCBWorld().getUID()) {
                event.setTo(event.getPlayer().getBedSpawnLocation());
            } else {
                event.setTo(toWorld.getSpawnLocation());
            }
        }
    }
}
