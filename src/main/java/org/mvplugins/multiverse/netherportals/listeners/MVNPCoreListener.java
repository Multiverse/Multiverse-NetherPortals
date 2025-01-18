package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.bukkit.event.EventHandler;
import org.mvplugins.multiverse.core.api.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.api.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.api.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;

import java.io.File;

@Service
public class MVNPCoreListener implements MVNPListener {

    private final MultiverseNetherPortals plugin;

    @Inject
    MVNPCoreListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is called when Multiverse-Core wants to reload config files.
     *
     * @param event The Config Reload event.
     */
    @EventHandler
    public void configReloadEvent(MVConfigReloadEvent event) {
        this.plugin.loadConfig();
        event.addConfig("Multiverse-NetherPortals - config.yml");
    }

    /**
     * This method is called when Multiverse-Core wants version info.
     *
     * @param event The Version event.
     */
    @EventHandler
    public void versionEvent(MVDumpsDebugInfoEvent event) {
        event.appendDebugInfo(this.plugin.getDebugInfo());
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        event.putDetailedDebugInfo("multiverse-netherportals/config.yml", configFile);
    }

    /**
     * This method is called when Multiverse-Core changes the debug mode.
     *
     * @param event The Debug Mode event.
     */
    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
