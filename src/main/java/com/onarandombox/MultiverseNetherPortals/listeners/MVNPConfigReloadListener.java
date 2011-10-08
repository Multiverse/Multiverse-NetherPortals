package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionRequestEvent;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class MVNPConfigReloadListener extends CustomEventListener {
    private MultiverseNetherPortals plugin;

    public MVNPConfigReloadListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCustomEvent(Event event) {
        if (event.getEventName().equals("MVConfigReload") && event instanceof MVConfigReloadEvent) {
            plugin.loadConfig();
            ((MVConfigReloadEvent) event).addConfig("Multiverse-NetherPortals - config.yml");
        } else if (event.getEventName().equals("MVVersion") && event instanceof MVVersionRequestEvent) {
            ((MVVersionRequestEvent) event).setPasteBinBuffer(this.plugin.dumpVersionInfo(((MVVersionRequestEvent) event).getPasteBinBuffer()));
        }
    }
}
