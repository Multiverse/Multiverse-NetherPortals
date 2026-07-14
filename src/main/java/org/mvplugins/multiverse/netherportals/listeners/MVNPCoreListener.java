package org.mvplugins.multiverse.netherportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.core.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import java.io.File;

@Service
final class MVNPCoreListener implements MVNPListener {

    private final MultiverseNetherPortals plugin;
    private final NetherPortalsConfig config;
    private final LinksManager linksManager;

    @Inject
    MVNPCoreListener(MultiverseNetherPortals plugin, NetherPortalsConfig config, LinksManager linksManager) {
        this.plugin = plugin;
        this.config = config;
        this.linksManager = linksManager;
    }

    /**
     * This method is called when Multiverse-Core wants to reload config files.
     *
     * @param event The Config Reload event.
     */
    @EventMethod
    public void configReloadEvent(MVConfigReloadEvent event) {
        this.config.load()
                .flatMap(ignored -> linksManager.load())
                .flatMap(ignored -> linksManager.save())
                .flatMap(ignored -> config.save());
        event.addConfig("Multiverse-NetherPortals - config.yml");
        event.addConfig("Multiverse-NetherPortals - links.yml");
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
        File linksFile = new File(this.plugin.getDataFolder(), "links.yml");
        event.putDetailedDebugInfo("multiverse-netherportals/links.yml", linksFile);
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
