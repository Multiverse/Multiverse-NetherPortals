package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
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
import org.bukkit.event.entity.EntityPortalEvent;
import java.util.logging.Level;

public class MVNPEntityListener implements Listener {

    private MultiverseNetherPortals plugin;
    private MVNameChecker nameChecker;
    private MVLinkChecker linkChecker;
    private MVWorldManager worldManager;

    public MVNPEntityListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(this.plugin);
        this.linkChecker = new MVLinkChecker(this.plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Location currentLocation = event.getFrom().clone();
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }
        String currentWorld = currentLocation.getWorld().getName();

        PortalType type = PortalType.END;
        if (event.getFrom().getBlock().getType() == Material.PORTAL) {
            type = PortalType.NETHER;
            event.useTravelAgent(true);
        }

        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);

        if (linkedWorld != null) {
            this.linkChecker.findNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                this.plugin.log(Level.FINER, "");
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER));
            } else {
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)));
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.END)));
            } else {
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.END));
            }
        } else {
            if(type == PortalType.END) {
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getEndName(currentWorld));
            } else {
                this.linkChecker.findNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(currentWorld));
            }
        }
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (event.getTo() == null || event.getFrom() == null) {
            event.setCancelled(true);
            return;
        }
        if (fromWorld.getCBWorld().equals(toWorld.getCBWorld())) {
            event.setCancelled(true);
            return;
        }
        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
            event.getPortalTravelAgent().setCanCreatePortal(false);
            event.setTo(toWorld.getSpawnLocation());
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
            event.getPortalTravelAgent().setCanCreatePortal(true);
            event.setTo(event.getPortalTravelAgent().findOrCreate(event.getTo()));
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
