package com.onarandombox.MultiverseNetherPortals.utils;

import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.runnables.PlayerTouchingPortalTask;
import org.bukkit.PortalType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MVEventRecord {
    private final MultiverseNetherPortals plugin;
    private final Map<UUID, BukkitTask> ender;
    private final Map<UUID, BukkitTask> nether;

    public MVEventRecord(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.ender = new HashMap<>();
        this.nether = new HashMap<>();
    }

    private Map<UUID, BukkitTask> getPortalRecord(PortalType type) {
        switch (type) {
            case ENDER:
                return this.ender;
            case NETHER:
                return this.nether;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Checks whether the given Player UUID is present in the specified
     * PortalType's event record.
     *
     * @param portalType {@code PortalType.ENDER} or {@code PortalType.NETHER}.
     * @param uuid       The UUID to check.
     * @return           True if the UUID is present in the event record.
     */
    public boolean isInRecord(PortalType portalType, UUID uuid) {
        Map<UUID, BukkitTask> portalRecord = this.getPortalRecord(portalType);
        return portalRecord.containsKey(uuid);
    }

    /**
     * Adds the given Player UUID to the specified PortalType's event record.
     * This also creates a task that will run every 200 ticks (10 seconds)
     * which will remove the UUID from the event record if the player is
     * no longer standing inside a portal.
     *
     * @param portalType {@code PortalType.ENDER} or {@code PortalType.NETHER}.
     * @param uuid       The UUID to add to the event record.
     */
    public void addToRecord(PortalType portalType, UUID uuid) {
        Map<UUID, BukkitTask> portalRecord = this.getPortalRecord(portalType);

        if (portalRecord.containsKey(uuid)) {
            return;
        }

        portalRecord.put(uuid, new PlayerTouchingPortalTask(this, portalType, uuid).runTaskTimer(this.plugin, 200L, 200L));
    }

    /**
     * Removes the given Player UUID from the specified PortalType's event record
     * if present. Additionally, the task made when adding the UUID to the event
     * record will be cancelled.
     *
     * @param portalType {@code PortalType.ENDER} or {@code PortalType.NETHER}.
     * @param uuid       The UUID to remove from the event record.
     */
    public void removeFromRecord(PortalType portalType, UUID uuid) {
        Map<UUID, BukkitTask> portalRecord = this.getPortalRecord(portalType);

        if (portalRecord.containsKey(uuid)) {
            portalRecord.remove(uuid).cancel();
        }
    }
}
