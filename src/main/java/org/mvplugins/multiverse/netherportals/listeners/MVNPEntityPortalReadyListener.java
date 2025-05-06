package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.EventRunnable;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventClass;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.utils.MVNameChecker;

@Service
final class MVNPEntityPortalReadyListener implements MVNPListener {

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;

    @Inject
    MVNPEntityPortalReadyListener(@NotNull MultiverseNetherPortals plugin, @NotNull MVNameChecker nameChecker) {
        this.plugin = plugin;
        this.nameChecker = nameChecker;
    }

    @EventClass("io.papermc.paper.event.entity.EntityPortalReadyEvent")
    private EventRunnable entityPortalReadyEvent() {
        return new EventRunnable<EntityPortalReadyEvent>() {
            public void onEvent(EntityPortalReadyEvent event) {
                String fromWorld = event.getEntity().getWorld().getName();
                String linkedWorld = getLinkedWorld(fromWorld, event.getPortalType());
                if (linkedWorld == null) {
                    Logging.fine("No linked world found for world '%s'", fromWorld);
                    return;
                }
                World bukkitLinkedWorld = Bukkit.getWorld(linkedWorld);
                if (bukkitLinkedWorld == null) {
                    Logging.fine("Target linked world '%s' not a loaded bukkit world.", linkedWorld);
                    return;
                }
                Logging.fine("Found linked world '%s' for world '%s'", linkedWorld, fromWorld);
                event.setTargetWorld(bukkitLinkedWorld);
            }
        };
    }

    private String getLinkedWorld(String currentWorld, PortalType type) {
        String linkedWorld = plugin.getWorldLink(currentWorld, type);
        if (linkedWorld != null) {
            Logging.finer("Got manually linked world '%s' for world '%s'", linkedWorld, currentWorld);
            return linkedWorld;
        }
        if (nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.ENDER) {
                return nameChecker.getNormalName(currentWorld, type);
            } else if (type == PortalType.NETHER) {
                return nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, type));
            }
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.ENDER) {
                return nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, type));
            } else if (type == PortalType.NETHER) {
                return nameChecker.getNormalName(currentWorld, type);
            }
        } else {
            if (type == PortalType.ENDER) {
                return nameChecker.getEndName(currentWorld);
            } else if (type == PortalType.NETHER) {
                return nameChecker.getNetherName(currentWorld);
            }
        }
        return null;
    }
}
