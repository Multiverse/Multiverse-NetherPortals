package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MVNPEntityListener implements Listener {

    private MultiverseNetherPortals plugin;
    private MVLinkChecker linkChecker;
    private MVWorldManager worldManager;
    private PermissionTools pt;
    private int cooldown = 250;
    private Map<String, Date> playerErrors;
    private Map<String, Location> eventRecord;
    private LocationManipulation locationManipulation;
    // This hash map will track players most recent portal touch.
    // we can use this cache to avoid a TON of unrequired calls to the
    // On entity portal touch calculations.

    public MVNPEntityListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.linkChecker = new MVLinkChecker(this.plugin);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.playerErrors = new HashMap<String, Date>();
        this.eventRecord = new HashMap<String, Location>();
        this.locationManipulation = this.plugin.getCore().getLocationManipulation();

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

        if(this.eventRecord.containsKey(p.getName())) {
            // The the eventRecord shows this player was already trying to go somewhere.
            if (this.locationManipulation.getBlockLocation(p.getLocation()).equals(this.eventRecord.get(p.getName()))) {
                // The player has not moved, and we've already fired one event.
                return;
            } else {
                // The player moved, potentially out of the portal, allow event to re-check.
                this.eventRecord.put(p.getName(), this.locationManipulation.getBlockLocation(p.getLocation()));
                // We'll need to clear this value...
            }
        } else {
            this.eventRecord.put(p.getName(), this.locationManipulation.getBlockLocation(p.getLocation()));
        }
        MVPlayerTouchedPortalEvent playerTouchedPortalEvent = new MVPlayerTouchedPortalEvent(p, event.getLocation());
        this.plugin.getServer().getPluginManager().callEvent(playerTouchedPortalEvent);
        if(playerTouchedPortalEvent.isCancelled()) {
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

        PortalType type = PortalType.END; //we are too lazy to check if it's this one
        if (event.getLocation().getBlock().getType() == Material.PORTAL) {
            type = PortalType.NETHER;
        }

        String linkedWorld = this.plugin.getWorldLink(event.getLocation().getWorld().getName(), type);
        Location currentLocation = event.getLocation();

        Location toLocation = null;

        if (linkedWorld != null) {
            toLocation = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, p);
        }

        if (toLocation == null) {
            return;
        }
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(p.getLocation().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(toLocation.getWorld().getName());
        if (!pt.playerHasMoneyToEnter(fromWorld, toWorld, p, p, false)) {
            System.out.println("BOOM");
            this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                    "' because they don't have the FUNDS required to enter.");
            return;
        }
        if (this.plugin.getCore().getMVConfig().getEnforceAccess()) {
            if (!pt.playerCanGoFromTo(fromWorld, toWorld, p, p)) {
                this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was DENIED ACCESS to '" + toWorld.getCBWorld().getName() +
                        "' because they don't have: multiverse.access." + toWorld.getCBWorld().getName());
            }
        } else {
            this.plugin.log(Level.FINE, "Player '" + p.getName() + "' was allowed to go to '" + toWorld.getCBWorld().getName() + "' because enforceaccess is off.");
        }
    }
}
