package org.mvplugins.multiverse.netherportals.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

@Service
final class MVNPPluginListener implements MVNPListener {

    private final MultiverseNetherPortals plugin;

    @Inject
    public MVNPPluginListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    @EventMethod
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Portals")) {
            this.plugin.setPortals(event.getPlugin());
        }
    }

    @EventMethod
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Portals")) {
            this.plugin.setPortals(null);
        }
    }
}
