package org.mvplugins.multiverse.netherportals;

import java.util.Map;
import java.util.logging.Logger;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.netherportals.command.MVNPCommandCompletions;
import org.mvplugins.multiverse.netherportals.commands.NetherPortalsCommand;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.listeners.MVNPListener;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.utils.customportals.CustomPortalsHandler;

public class MultiverseNetherPortals extends MultiverseModule {

    private static final double TARGET_CORE_API_VERSION = 5.0;

    @Inject
    private Provider<CoreConfig> coreConfig;
    @Inject
    private Provider<MVCommandManager> commandManager;
    @Inject
    private Provider<NetherPortalsConfig> netherPortalsConfig;
    @Inject
    private Provider<LinksManager> linksManager;
    @Inject
    private Provider<MVNPCommandCompletions> commandCompletionsProvider;
    @Inject
    private Provider<CustomPortalsHandler> customPortalsHandlerProvider;
    @Inject
    private Provider<BstatsMetricsConfigurator> metricsConfiguratorProvider;

    @Override
    public void onLoad() {
        super.onLoad();
        Logging.init(this);
        getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        initializeDependencyInjection(new MultiverseNetherPortalsPluginBinder(this));
        Logging.setDebugLevel(coreConfig.get().getGlobalDebug());

        if (!setupConfig()) {
            Logging.severe("Your configs were not loaded.");
            Logging.severe("Please check your configs and restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setUpLocales();
        commandCompletionsProvider.get();
        this.registerCommands(NetherPortalsCommand.class);
        this.registerDynamicListeners(MVNPListener.class);
        this.setupMetrics();
        MultiverseNetherPortalsApi.init(this);

        Logging.config("Version %s (API v%s) Enabled - By %s",
                this.getDescription().getVersion(), getVersionAsNumber(), StringFormatter.joinAnd(this.getDescription().getAuthors()));
    }

    private boolean setupConfig() {
        NetherPortalsConfig config = netherPortalsConfig.get();
        LinksManager links = linksManager.get();
        return config.load()
                .flatMap(ignored -> links.load())
                .flatMap(ignored -> links.save())
                .flatMap(ignored -> config.save())
                .isSuccess()
                && config.isLoaded()
                && links.isLoaded();
    }

    /**
     * Setup bstats Metrics.
     */
    private void setupMetrics() {
        Try.of(() -> metricsConfiguratorProvider.get())
                .onFailure(e -> {
                    Logging.severe("Failed to setup metrics");
                    e.printStackTrace();
                });
    }

    @Override
    public void onDisable() {
        MultiverseNetherPortalsApi.shutdown();
        if (netherPortalsConfig != null && linksManager != null) {
            linksManager.get().save().flatMap(ignored -> netherPortalsConfig.get().save());
        }
        shutdownDependencyInjection();
        Logging.info("- Disabled");
        Logging.shutdown();
    }

    @Override
    public double getTargetCoreVersion() {
        return TARGET_CORE_API_VERSION;
    }

    @Override
    public @NotNull Logger getLogger() {
        return Logging.getLogger();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#load()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void loadConfig() {
        setupConfig();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setNetherPrefix(String)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setNetherPrefix(String netherPrefix) {
        netherPortalsConfig.get().setNetherPrefix(netherPrefix);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#getNetherPrefix()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getNetherPrefix() {
        return netherPortalsConfig.get().getNetherPrefix();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setNetherSuffix(String)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setNetherSuffix(String netherSuffix) {
        netherPortalsConfig.get().setNetherSuffix(netherSuffix);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#getNetherSuffix()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getNetherSuffix() {
        return netherPortalsConfig.get().getNetherSuffix();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setEndPrefix(String)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setEndPrefix(String endPrefix) {
        netherPortalsConfig.get().setEndPrefix(endPrefix);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#getEndPrefix()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getEndPrefix() {
        return netherPortalsConfig.get().getEndPrefix();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setEndSuffix(String)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setEndSuffix(String endSuffix) {
        netherPortalsConfig.get().setEndSuffix(endSuffix);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#getEndSuffix()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getEndSuffix() {
        return netherPortalsConfig.get().getEndSuffix();
    }

    /**
     * @deprecated Use {@link LinksManager#getWorldLink(String, WorldLinkType)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getWorldLink(String fromWorld, PortalType type) {
        return WorldLinkType.fromPortalType(type)
                .flatMap(worldLinkType -> linksManager.get().getWorldLink(fromWorld, worldLinkType))
                .getOrNull();
    }

    /**
     * @deprecated Use {@link LinksManager#getLinksMapForType(WorldLinkType)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Map<String, String> getWorldLinks() {
        return linksManager.get().getLinksMapForType(WorldLinkType.NETHER);
    }

    /**
     * @deprecated Use {@link LinksManager#getLinksMapForType(WorldLinkType)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Map<String, String> getEndWorldLinks() {
        return linksManager.get().getLinksMapForType(WorldLinkType.END);
    }

    /**
     * @deprecated Use {@link LinksManager#addWorldLink(String, String, WorldLinkType)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean addWorldLink(String from, String to, PortalType type) {
        return WorldLinkType.fromPortalType(type)
                .map(worldLinkType -> linksManager.get().addWorldLink(from, to, worldLinkType)
                        && linksManager.get().save().isSuccess())
                .getOrElse(false);
    }

    /**
     * @deprecated Use {@link LinksManager#removeWorldLink(String, WorldLinkType)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean removeWorldLink(String from, String to, PortalType type) {
        return WorldLinkType.fromPortalType(type)
                .map(worldLinkType -> linksManager.get().removeWorldLink(from, worldLinkType)
                        && linksManager.get().save().isSuccess())
                .getOrElse(false);
    }

    /**
     * @deprecated Use {@link LinksManager#save()} and {@link NetherPortalsConfig#save()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean saveMVNPConfig() {
        return linksManager.get().save()
                .flatMap(ignored -> netherPortalsConfig.get().save())
                .isSuccess();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#isUsingBounceBack()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isUsingBounceBack() {
        return netherPortalsConfig.get().isUsingBounceBack();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setUsingBounceBack(boolean)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setUsingBounceBack(boolean useBounceBack) {
        netherPortalsConfig.get().setUsingBounceBack(useBounceBack);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#isTeleportingEntities()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isTeleportingEntities() {
        return netherPortalsConfig.get().isTeleportingEntities();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setTeleportingEntities(boolean)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setTeleportingEntities(boolean teleportingEntities) {
        netherPortalsConfig.get().setTeleportingEntities(teleportingEntities);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#isSendingDisabledPortalMessage()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isSendingDisabledPortalMessage() {
        return netherPortalsConfig.get().isSendingDisabledPortalMessage();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setSendingDisabledPortalMessage(boolean)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setSendingDisabledPortalMessage(boolean sendDisabledPortalMessage) {
        netherPortalsConfig.get().setSendingDisabledPortalMessage(sendDisabledPortalMessage);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#isSendingNoDestinationMessage()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isSendingNoDestinationMessage() {
        return netherPortalsConfig.get().isSendingNoDestinationMessage();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setSendingNoDestinationMessage(boolean)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setSendingNoDestinationMessage(boolean sendNoDestinationMessage) {
        netherPortalsConfig.get().setSendingNoDestinationMessage(sendNoDestinationMessage);
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#isEndPlatformDropBlocks()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isEndPlatformDropBlocks() {
        return netherPortalsConfig.get().isEndPlatformDropBlocks();
    }

    /**
     * @deprecated Use {@link NetherPortalsConfig#setEndPlatformDropBlocks(boolean)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public void setEndPlatformDropBlocks(boolean endPlatformDropBlocks) {
        netherPortalsConfig.get().setEndPlatformDropBlocks(endPlatformDropBlocks);
    }

    /**
     * @deprecated Use {@link CustomPortalsHandler#isHandledByCustomPortals(org.bukkit.entity.Entity, org.bukkit.Location)} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isHandledByNetherPortals(Location l) {
        return !customPortalsHandlerProvider.get().isHandledByCustomPortals(null, l);
    }

    /**
     * @deprecated Debug info no longer required as dumps command directly uploads all config files.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public String getDebugInfo() {
        NetherPortalsConfig config = netherPortalsConfig.get();
        LinksManager links = linksManager.get();
        return "[Multiverse-NetherPortals] Multiverse-NetherPortals Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-NetherPortals] Nether Prefix: " + config.getNetherPrefix() + '\n'
                + "[Multiverse-NetherPortals] Nether Suffix: " + config.getNetherSuffix() + '\n'
                + "[Multiverse-NetherPortals] End Prefix: " + config.getEndPrefix() + '\n'
                + "[Multiverse-NetherPortals] End Suffix: " + config.getEndSuffix() + '\n'
                + "[Multiverse-NetherPortals] Nether Links: " + links.getLinksMapForType(WorldLinkType.NETHER) + '\n'
                + "[Multiverse-NetherPortals] End Links: " + links.getLinksMapForType(WorldLinkType.END) + '\n'
                + "[Multiverse-NetherPortals] Bounceback: " + config.isUsingBounceBack() + '\n'
                + "[Multiverse-NetherPortals] Teleport Entities: " + config.isTeleportingEntities() + '\n'
                + "[Multiverse-NetherPortals] Send Disabled Portal Message: "
                + config.isSendingDisabledPortalMessage() + '\n'
                + "[Multiverse-NetherPortals] Send No Destination Message: "
                + config.isSendingNoDestinationMessage() + '\n'
                + "[Multiverse-NetherPortals] Server Allow Nether: " + this.getServer().getAllowNether() + '\n'
                + "[Multiverse-NetherPortals] Server Allow End: " + this.getServer().getAllowEnd() + '\n'
                + "[Multiverse-NetherPortals] Special Code: " + "FRN001" + '\n';
    }
}
