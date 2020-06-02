package com.onarandombox.MultiverseNetherPortals.runnables;

import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTouchingPortalTask extends BukkitRunnable {
    private final ConcurrentHashMap<PortalType, Set<Player>> eventRecord;
    private final PortalType portalType;
    private final Player player;

    public PlayerTouchingPortalTask(ConcurrentHashMap<PortalType, Set<Player>> eventRecord, PortalType portalType, Player player) {
        this.eventRecord = eventRecord;
        this.portalType = portalType;
        this.player = player;
    }

    @Override
    public void run() {
        PortalType type = null;

        if (player.getLocation().getBlock().getType() == Material.END_PORTAL) type = PortalType.ENDER;
        else if (player.getLocation().getBlock().getType() == Material.NETHER_PORTAL) type = PortalType.NETHER;

        if (!player.isOnline() || type != this.portalType) {
            eventRecord.get(this.portalType).remove(this.player);
            this.cancel();
        }
    }
}
