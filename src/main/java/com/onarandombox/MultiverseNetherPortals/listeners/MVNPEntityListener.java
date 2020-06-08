package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.runnables.PlayerTouchingPortalTask;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MVNPEntityListener implements Listener {

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final MVWorldManager worldManager;
    private final PermissionTools pt;
    private final int cooldown = 250;
    private final MultiverseMessaging messaging;
    private final Map<String, Date> playerErrors;
    private final LocationManipulation locationManipulation;
    private final ConcurrentHashMap<PortalType, Set<Player>> eventRecord;
    // This map will track whether each player is touching a portal.
    // We can use this to avoid lots of unnecessary calls to the
    // on entity portal touch calculations.

    public MVNPEntityListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = new MVNameChecker(this.plugin);
        this.linkChecker = new MVLinkChecker(this.plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.playerErrors = new HashMap<String, Date>();
        this.messaging = this.plugin.getCore().getMessaging();
        this.locationManipulation = this.plugin.getCore().getLocationManipulation();
        this.eventRecord = new ConcurrentHashMap<>();
        this.eventRecord.put(PortalType.ENDER, ConcurrentHashMap.newKeySet());
        this.eventRecord.put(PortalType.NETHER, ConcurrentHashMap.newKeySet());
    }

    protected boolean shootPlayer(Player p, Block block, PortalType type) {
        if (!plugin.isUsingBounceBack()) {
            this.plugin.log(Level.FINEST, "Bounceback is disabled, so the player is free to walk into portals!");
            return false;
        }

        this.playerErrors.put(p.getName(), new Date());
        double newVecX = 0;
        double newVecZ = 0;
        double strength = 2;
        boolean playerBounced = false;

        StringBuilder debugMessage = new StringBuilder().append("Player: ").append(p.getName());
        if (type == PortalType.ENDER) {
            debugMessage.append(" entered an End Portal. There is currently no bounceback implementation for End Portals.");
        } else if (type == PortalType.NETHER) {
            // Determine portal axis:
            if (block.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL || block.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL) {
                // we add 0.5 to the location of the block to get the center
                if (p.getLocation().getZ() < block.getLocation().getZ() + 0.5) {
                    debugMessage.append(" entered Nether Portal from the North");
                    newVecZ = -1 * strength;
                } else {
                    debugMessage.append(" entered Nether Portal from the South");
                    newVecZ = 1 * strength;
                }
            } else {
                // we add 0.5 to the location of the block to get the center
                if (p.getLocation().getX() < block.getLocation().getX() + 0.5) {
                    debugMessage.append(" entered Nether Portal from the West");
                    newVecX = -1 * strength;
                } else {
                    debugMessage.append(" entered Nether Portal from the East");
                    newVecX = 1 * strength;
                }
            }

            debugMessage.append(". They will be bounced back!");
            p.teleport(p.getLocation().clone().add(newVecX, .2, newVecZ));
            p.setVelocity(new Vector(newVecX, .6, newVecZ));
            playerBounced = true;
        }

        this.plugin.log(Level.FINER, debugMessage.toString());
        return playerBounced;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) event.getEntity();
        Location block = this.locationManipulation.getBlockLocation(p.getLocation());

        if (!plugin.isHandledByNetherPortals(block)) {
            return;
        }

        PortalType type;
        // determine what kind of portal the player is using
        if (block.getBlock().getType() == Material.END_PORTAL) type = PortalType.ENDER;
        else if (block.getBlock().getType() == Material.NETHER_PORTAL) type = PortalType.NETHER;
        else return;

        BukkitTask isTouching;
        if (this.eventRecord.get(type).contains(p)) return;
        else {
            this.eventRecord.get(type).add(p);

            // this runnable will check if the player is still standing in the portal
            // if they aren't, it will remove them from the event record
            isTouching = new PlayerTouchingPortalTask(eventRecord, type, p).runTaskTimer(this.plugin, 200L, 200L);
        }

        MVPlayerTouchedPortalEvent playerTouchedPortalEvent = new MVPlayerTouchedPortalEvent(p, event.getLocation());
        this.plugin.getServer().getPluginManager().callEvent(playerTouchedPortalEvent);
        Location eventLocation = event.getLocation().clone();
        if (!playerTouchedPortalEvent.canUseThisPortal()) {
            // Someone else said the player is not allowed to go here.
            this.shootPlayer(p, eventLocation.getBlock(), type);
            this.plugin.log(Level.FINEST, "Someone requested that this player be bounced back!");
        }
        if (playerTouchedPortalEvent.isCancelled()) {
            this.plugin.log(Level.FINEST, "Someone cancelled the enter Event for NetherPortals!");
            return;
        }

        if (this.playerErrors.containsKey(p.getName())) {
            Date lastTry = this.playerErrors.get(p.getName());
            if (lastTry.getTime() + this.cooldown > new Date().getTime()) {
                return;
            }
            this.playerErrors.remove(p.getName());
        }

        String currentWorld = event.getLocation().getWorld().getName();
        String linkedWorld = this.plugin.getWorldLink(event.getLocation().getWorld().getName(), type);
        Location currentLocation = event.getLocation();

        Location toLocation;

        if (linkedWorld != null) {
            toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, p);
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER), p);
            } else {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)), p);
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.ENDER)), p);
            } else {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.ENDER), p);
            }
        } else {
            if (type == PortalType.ENDER) {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(currentWorld), p);
            } else {
                toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(currentWorld), p);
            }
        }

        if (toLocation == null) {
            if (this.shootPlayer(p, eventLocation.getBlock(), type)) {
                isTouching.cancel();
                this.eventRecord.get(type).remove(p);
            }
            if (this.plugin.isSendingNoDestinationMessage()) {
                this.messaging.sendMessage(p, "This portal goes nowhere!", false);
                if (type == PortalType.ENDER) {
                    this.messaging.sendMessage(p, "No specific end world has been linked to this world and '" + this.nameChecker.getEndName(currentWorld) + "' is not a world.", true);
                } else {
                    this.messaging.sendMessage(p, "No specific nether world has been linked to this world and '" + this.nameChecker.getNetherName(currentWorld) + "' is not a world.", true);
                }
            }
            return;
        }

        MultiverseWorld fromWorld = this.worldManager.getMVWorld(p.getLocation().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(toLocation.getWorld().getName());

        if (fromWorld.getCBWorld().equals(toWorld.getCBWorld())) {
            // The player is Portaling to the same world.
            this.plugin.log(Level.FINER, "Player '" + p.getName() + "' is portaling to the same world.");
            return;
        }
        if (!pt.playerHasMoneyToEnter(fromWorld, toWorld, p, p, false)) {
            this.shootPlayer(p, eventLocation.getBlock(), type);
            this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                    "' because they don't have the FUNDS required to enter.");
            return;
        }

        if (this.plugin.getCore().getMVConfig().getEnforceAccess()) {
            if (!pt.playerCanGoFromTo(fromWorld, toWorld, p, p)) {
                this.shootPlayer(p, eventLocation.getBlock(), type);
                this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                        "' because they don't have: multiverse.access." + toWorld.getCBWorld().getName());
            }
        } else {
            this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was allowed to go to '" + toWorld.getCBWorld().getName() + "' because enforceaccess is off.");
        }
    }

    public void onEntityPortalExit(EntityPortalExitEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            this.eventRecord.get(PortalType.ENDER).remove(p);
            this.eventRecord.get(PortalType.NETHER).remove(p);
        }
    }
}