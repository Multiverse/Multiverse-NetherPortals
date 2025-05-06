package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.core.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import java.io.File;

@Service
final class MVNPCoreListener implements MVNPListener {

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
    @EventMethod
    public void configReloadEvent(MVConfigReloadEvent event) {
        this.plugin.loadConfig();
        event.addConfig("Multiverse-NetherPortals - config.yml");
    }

    /**
     * This method is called when Multiverse-Core wants version info.
     *
     * @param event The Version event.
     */
    @EventMethod
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
    @EventMethod
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
