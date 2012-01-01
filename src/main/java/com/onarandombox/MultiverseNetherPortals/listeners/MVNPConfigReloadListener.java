package com.onarandombox.MultiverseNetherPortals.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
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
         if (event.getEventName().equals("MVVersionEvent") && event instanceof MVVersionEvent) {
            ((MVVersionEvent) event).appendVersionInfo(this.plugin.getVersionInfo());
        }
    }
}
