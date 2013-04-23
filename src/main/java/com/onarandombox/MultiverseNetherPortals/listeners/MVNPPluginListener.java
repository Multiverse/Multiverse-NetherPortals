package com.onarandombox.MultiverseNetherPortals.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;

public class MVNPPluginListener implements Listener {

    private MultiverseNetherPortals plugin;

    public MVNPPluginListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        }
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Portals")) {
            this.plugin.setPortals(event.getPlugin());
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(null);
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Portals")) {
            this.plugin.setPortals(null);
        }
    }
}
