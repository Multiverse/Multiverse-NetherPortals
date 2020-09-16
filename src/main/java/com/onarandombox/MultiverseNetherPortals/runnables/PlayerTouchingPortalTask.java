package com.onarandombox.MultiverseNetherPortals.runnables;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerTouchingPortalTask extends BukkitRunnable {
    private final Map<PortalType, Set<UUID>> eventRecord;
    private final PortalType portalType;
    private final UUID uuid;

    public PlayerTouchingPortalTask(Map<PortalType, Set<UUID>> eventRecord, PortalType portalType, UUID uuid) {
        this.eventRecord = eventRecord;
        this.portalType = portalType;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        PortalType type = null;
        Player p = Bukkit.getPlayer(this.uuid);

        if (p != null && !p.isOnline()) {
            if (p.getLocation().getBlock().getType() == Material.END_PORTAL) {
                type = PortalType.ENDER;
            } else if (p.getLocation().getBlock().getType() == Material.NETHER_PORTAL) {
                type = PortalType.NETHER;
            }
        }

        if (type != this.portalType) {
            this.eventRecord.get(this.portalType).remove(this.uuid);
            this.cancel();
        }
    }
}
