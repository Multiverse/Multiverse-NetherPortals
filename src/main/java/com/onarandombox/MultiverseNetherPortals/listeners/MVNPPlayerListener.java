package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.util.Vector;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MVNPPlayerListener implements Listener {

    private MultiverseNetherPortals plugin;
    private MultiverseMessaging messaging;
    private MVNameChecker nameChecker;
    private MVLinkChecker linkChecker;
    private MVWorldManager worldManager;
    private Map<String, Date> playerErrors;
    private PermissionTools pt;

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.playerErrors = new HashMap<String, Date>();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.linkChecker = new MVLinkChecker(this.plugin);
        this.messaging = this.plugin.getCore().getMessaging();
    }

    protected void shootPlayer(Player p, Block block, PortalType type) {
        if (!plugin.isUsingBounceBack()) {
            this.plugin.log(Level.FINEST, "You said not to use bounce back so the player is free to walk into portal!");
            return;
        }
        this.playerErrors.put(p.getName(), new Date());
        double myconst = 2;
        double newVecX = 0;
        double newVecZ = 0;
        if (block.getRelative(BlockFace.EAST).getType() == Material.PORTAL || block.getRelative(BlockFace.WEST).getType() == Material.PORTAL) {
            this.plugin.log(Level.FINER, "Found Portal: East/West");
            if (p.getLocation().getX() < block.getLocation().getX()) {
                newVecZ = -1 * myconst;
            } else {
                newVecZ = 1 * myconst;
            }
        } else {
            //NOrth/South
            this.plugin.log(Level.FINER, "Found Portal: North/South");
            if (p.getLocation().getZ() < block.getLocation().getZ()) {
                newVecX = -1 * myconst;
            } else {
                newVecX = 1 * myconst;
            }
        }
        p.teleport(p.getLocation().clone().add(newVecX, .2, newVecZ));
        p.setVelocity(new Vector(newVecX, .6, newVecZ));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            this.plugin.log(Level.FINEST, "PlayerPortalEvent was cancelled! NOT teleporting!");
            return;
        }
        Location currentLocation = event.getFrom().clone();
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }
        String currentWorld = currentLocation.getWorld().getName();

        PortalType type = null;
        if (currentLocation.getWorld().getBlockAt(currentLocation).getType() == Material.ENDER_PORTAL) {
            type = PortalType.END;
        } else {
            type = PortalType.NETHER;
            event.useTravelAgent(true);
        }

        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);

        if (linkedWorld != null) {
            this.linkChecker.getNewTeleportLocation(event, currentLocation, linkedWorld);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
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
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (event.getTo() == null || event.getFrom() == null) {
            this.shootPlayer(event.getPlayer(), currentLocation.getBlock(), type);
            if (plugin.isUsingBounceBack()) {
                this.messaging.sendMessage(event.getPlayer(), "This portal goes nowhere!", false);
                if (type == PortalType.END) {
                    this.messaging.sendMessage(event.getPlayer(), "No specific end world has been linked to this world and '" + this.nameChecker.getEndName(currentWorld) + "' is not a world.", true);
                } else {
                    this.messaging.sendMessage(event.getPlayer(), "No specific nether world has been linked to this world and '" + this.nameChecker.getNetherName(currentWorld) + "' is not a world.", true);
                }
            }
            event.setCancelled(true);
            return;
        }
        if (fromWorld.getCBWorld().equals(toWorld.getCBWorld())) {
            this.shootPlayer(event.getPlayer(), currentLocation.getBlock(), type);
            if (plugin.isUsingBounceBack()) {
                this.messaging.sendMessage(event.getPlayer(), "This portal goes nowhere!", false);
                if (type == PortalType.END) {
                    this.messaging.sendMessage(event.getPlayer(), "No specific end world has been linked to this world and '" + this.nameChecker.getEndName(currentWorld) + "' is not a world.", true);
                } else {
                    this.messaging.sendMessage(event.getPlayer(), "No specific nether world has been linked to this world and '" + this.nameChecker.getNetherName(currentWorld) + "' is not a world.", true);
                }
            }
            // The player is Portaling to the same world.
            this.plugin.log(Level.FINER, "Player '" + event.getPlayer().getName() + "' is portaling to the same world.");
            event.setCancelled(true);
            return;
        }
        if (!pt.playerHasMoneyToEnter(fromWorld, toWorld, event.getPlayer(), event.getPlayer(), false)) {
            this.shootPlayer(event.getPlayer(), currentLocation.getBlock(), type);
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                    "' because they don't have the FUNDS required to enter.");
            return;
        }
        if (this.plugin.getCore().getMVConfig().getEnforceAccess()) {
            if (!pt.playerCanGoFromTo(fromWorld, toWorld, event.getPlayer(), event.getPlayer())) {
                this.shootPlayer(event.getPlayer(), currentLocation.getBlock(), type);
                this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                        "' because they don't have: multiverse.access." + toWorld.getCBWorld().getName());
                return;
            }
        } else {
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was allowed to go to '" + toWorld.getCBWorld().getName() + "' because enforceaccess is off.");
        }
        if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
            this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' will be teleported to the spawn of '" + toWorld.getName() + "' since they used an end exit portal.");
            event.getPortalTravelAgent().setCanCreatePortal(false);
            if (toWorld.getBedRespawn()
                    && event.getPlayer().getBedSpawnLocation() != null
                    && event.getPlayer().getBedSpawnLocation().getWorld().getUID() == toWorld.getCBWorld().getUID()) {
                event.setTo(event.getPlayer().getBedSpawnLocation());
            } else {
                event.setTo(toWorld.getSpawnLocation());
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
            event.getPortalTravelAgent().setCanCreatePortal(true);
            event.setTo(event.getPortalTravelAgent().findOrCreate(event.getTo()));
        } else if (toWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.END) {
            Location loc = new Location(event.getTo().getWorld(), 100, 50, 0); // This is the vanilla location for obsidian platform.
            event.setTo(loc);
            Block block = loc.getBlock();
            for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                    Block platformBlock = loc.getWorld().getBlockAt(x, block.getY() - 2, z);
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
