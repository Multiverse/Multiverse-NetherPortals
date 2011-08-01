package com.onarandombox.MultiverseNetherPortals.listeners;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;       
import com.onarandombox.MultiverseCore.event.MVVersionRequestEvent;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;

public class MVNPConfigReloadListener extends CustomEventListener {
    private MultiverseNetherPortals plugin;
    public MVNPConfigReloadListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onCustomEvent(Event event) {
        if(event instanceof MVConfigReloadEvent) {
            plugin.loadConfig();
            ((MVConfigReloadEvent)event).addConfig("Multiverse-NetherPortals - config.yml");
        } else if(event instanceof MVVersionRequestEvent) {
            plugin.dumpVersionInfo();
        }
    }
}
