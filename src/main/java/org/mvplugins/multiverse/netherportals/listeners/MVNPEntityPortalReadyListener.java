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
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.utils.MVLinkChecker;
import org.mvplugins.multiverse.netherportals.utils.MVNameChecker;

@Service
final class MVNPEntityPortalReadyListener implements MVNPListener {

    private final LinksManager linksManager;
    private final MVLinkChecker linkChecker;

    @Inject
    MVNPEntityPortalReadyListener(@NotNull LinksManager linksManager, @NotNull MVLinkChecker linkChecker) {
        this.linksManager = linksManager;
        this.linkChecker = linkChecker;
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
        String linkedWorld = WorldLinkType.fromPortalType(type)
                .flatMap(worldLinkType -> linksManager.getWorldLink(currentWorld, worldLinkType))
                .getOrNull();
        if (linkedWorld != null) {
            Logging.finer("Got manually linked world '%s' for world '%s'", linkedWorld, currentWorld);
            return linkedWorld;
        }
        return linkChecker.getAutoLink(currentWorld, type);
    }
}
