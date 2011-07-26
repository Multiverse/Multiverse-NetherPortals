package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;       

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
        }
    }
}
