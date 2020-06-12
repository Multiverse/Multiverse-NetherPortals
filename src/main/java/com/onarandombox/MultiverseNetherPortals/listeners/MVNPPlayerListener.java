package com.onarandombox.MultiverseNetherPortals.listeners;

import java.util.logging.Level;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MVNPPlayerListener implements Listener {

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final MVWorldManager worldManager;
    private final PermissionTools pt;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.linkChecker = new MVLinkChecker(this.plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location currentLocation = event.getFrom().clone();
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }

        PortalType type;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) type = PortalType.ENDER;
        else if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) type = PortalType.NETHER;
        else return;

        if (type == PortalType.NETHER) {
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.useTravelAgent(true);
            } catch (ClassNotFoundException ignore) {
                plugin.log(Level.FINE, "TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName());
            }
        }

        Location newTo;
        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);
        if (currentWorld.equalsIgnoreCase(linkedWorld)) {
            newTo = null;
        } else if (linkedWorld != null) {
            newTo = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, event.getPlayer());
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)), event.getPlayer());
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.ENDER)), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.ENDER), event.getPlayer());
            }
        } else {
            if (type == PortalType.ENDER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(currentWorld), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(currentWorld), event.getPlayer());
            }
        }

        if (newTo != null) event.setTo(newTo);
        else {
            event.setCancelled(true);
            return;
        }

        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (!event.isCancelled()) {
            if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
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
            } else if (toWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
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
