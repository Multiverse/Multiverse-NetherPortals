package com.onarandombox.MultiverseNetherPortals.listeners;

import java.util.logging.Level;

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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

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
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }
        String currentWorld = currentLocation.getWorld().getName();

        PortalType type = PortalType.END;
        if (event.getFrom().getBlock().getType() == Material.NETHER_PORTAL) {
            type = PortalType.NETHER;
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.useTravelAgent(true);
            } catch (ClassNotFoundException ignore) {
                plugin.log(Level.FINE, "TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName());
            }
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

        if (!event.isCancelled()) {
            if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
                this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' will be teleported to the spawn of '" + toWorld.getName() + "' since they used an end exit portal.");
                try {
                    Class.forName("org.bukkit.TravelAgent");
                    event.getPortalTravelAgent().setCanCreatePortal(false);
                } catch (ClassNotFoundException ignore) {
                    plugin.log(Level.FINE, "TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName() + ". There may be a portal created at spawn.");
                }
                if (toWorld.getBedRespawn()
                        && event.getPlayer().getBedSpawnLocation() != null
                        && event.getPlayer().getBedSpawnLocation().getWorld().getUID() == toWorld.getCBWorld().getUID()) {
                    event.setTo(event.getPlayer().getBedSpawnLocation());
                } else {
                    event.setTo(toWorld.getSpawnLocation());
                }
            } else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
                try {
                    Class.forName("org.bukkit.TravelAgent");
                    event.getPortalTravelAgent().setCanCreatePortal(true);
                    event.setTo(event.getPortalTravelAgent().findOrCreate(event.getTo()));
                } catch (ClassNotFoundException ignore) {
                    plugin.log(Level.FINE, "TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName() + ". Their destination may not be correct.");
                    event.setTo(event.getTo());
                }
            } else if (toWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
                Location loc = new Location(event.getTo().getWorld(), 100, 50, 0); // This is the vanilla location for obsidian platform.
                event.setTo(loc);
                Block block = loc.getBlock();
                for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                    for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                        Block platformBlock = loc.getWorld().getBlockAt(x, block.getY() - 1, z);
                        if (platformBlock.getType() != Material.OBSIDIAN) {
                            platformBlock.setType(Material.OBSIDIAN);
                        }
                        for (int yMod = 1; yMod <= 3; yMod++) {
                            Block b = platformBlock.getRelative(BlockFace.UP, yMod);
                            if (b.getType() != Material.AIR) {
                                b.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
