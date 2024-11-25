package org.mvplugins.multiverse.netherportals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.netherportals.commands.NetherPortalsCommand;
import org.mvplugins.multiverse.netherportals.listeners.MVNPListener;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.api.MVPlugin;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.config.MVCoreConfig;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.utils.PortalManager;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final String NETHER_PORTALS_CONFIG = "config.yml";
    private static final String DEFAULT_NETHER_PREFIX = "";
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private static final String DEFAULT_END_PREFIX = "";
    private static final String DEFAULT_END_SUFFIX = "_the_end";
    private final static int requiresProtocol = 24;
  
    protected MultiverseCore core;
    protected Plugin multiversePortals;
    protected FileConfiguration MVNPConfiguration;
    private Map<String, String> linkMap;
    private Map<String, String> endLinkMap;

    private PluginServiceLocator serviceLocator;
    @Inject
    private Provider<MVCoreConfig> mvCoreConfig;
    @Inject
    private Provider<MVCommandManager> commandManager;

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        Logging.init(this);
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        this.multiversePortals = getServer().getPluginManager().getPlugin("Multiverse-Portals");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            Logging.info("Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of NetherPortals requires Protocol Level: " + requiresProtocol);
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://dev.bukkit.org/bukkit-plugins/multiverse-core/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeDependencyInjection();
        Logging.setDebugLevel(mvCoreConfig.get().getGlobalDebug());

        this.core.incrementPluginCount();
        // As soon as we know MVCore was found, we can use the debug log!

        loadConfig();
        this.registerCommands();
        this.registerEvents();

        Logging.log(true, Level.INFO, " Enabled - By %s", getAuthors());
    }

    private void initializeDependencyInjection() {
        serviceLocator = core.getServiceLocatorFactory()
                .registerPlugin(new MultiverseNetherPortalsPluginBinder(this), core.getServiceLocator())
                .flatMap(PluginServiceLocator::enable)
                .getOrElseThrow(exception -> {
                    Logging.severe("Failed to initialize dependency injection!");
                    getServer().getPluginManager().disablePlugin(this);
                    return new RuntimeException(exception);
                });
    }

    public void loadConfig() {
        initMVNPConfig();

        this.linkMap = new HashMap<>();
        this.endLinkMap = new HashMap<>();

        this.setUsingBounceBack(this.isUsingBounceBack());
        this.setTeleportingEntities(this.isTeleportingEntities());
        this.setSendingNoDestinationMessage(this.isSendingNoDestinationMessage());
        this.setSendingDisabledPortalMessage(this.isSendingDisabledPortalMessage());
        this.setEndPlatformDropBlocks(this.isEndPlatformDropBlocks());

        this.setNetherPrefix(this.getNetherPrefix());
        this.setNetherSuffix(this.getNetherSuffix());
        this.setEndPrefix(this.getEndPrefix());
        this.setEndSuffix(this.getEndSuffix());
      
        if (this.MVNPConfiguration.getConfigurationSection("worlds") == null) {
            this.MVNPConfiguration.createSection("worlds");
        }
        Set<String> worldKeys = this.MVNPConfiguration.getConfigurationSection("worlds").getKeys(false);
        if (worldKeys != null) {
            for (String worldString : worldKeys) {
                String nether = this.MVNPConfiguration.getString("worlds." + worldString + ".portalgoesto." + PortalType.NETHER, null);
                String ender = this.MVNPConfiguration.getString("worlds." + worldString + ".portalgoesto." + PortalType.ENDER, null);
              
                if (nether != null) {
                    this.linkMap.put(worldString, nether);
                }
                if (ender != null) {
                    this.endLinkMap.put(worldString, ender);
                }

                // Convert from old version enum which used END not ENDER
                String oldEnder = this.MVNPConfiguration.getString("worlds." + worldString + ".portalgoesto.END", null);
                if (oldEnder != null) {
                    if (this.addWorldLink(worldString, oldEnder, PortalType.ENDER)) {
                        this.MVNPConfiguration.set("worlds." + worldString + ".portalgoesto.END", null);
                    }
                    else {
                        Logging.warning("Error converting old end link of '%s' to '%s'", worldString, oldEnder);
                    }
                }

            }
        }
        this.saveMVNPConfig();
    }

    private void initMVNPConfig() {
        this.MVNPConfiguration = new YamlConfiguration();
        try {
            File configFile = new File(this.getDataFolder(), NETHER_PORTALS_CONFIG);
            if (!configFile.isFile()) {
                Logging.info("Creating new %s", NETHER_PORTALS_CONFIG);
                configFile.createNewFile();
            }
            this.MVNPConfiguration.load(configFile);
        }
        catch (IOException e) {
            Logging.severe("Could not load " + NETHER_PORTALS_CONFIG);
        }
        catch (InvalidConfigurationException e) {
            Logging.severe(NETHER_PORTALS_CONFIG + " contained INVALID YAML. Please look at the file.");
        }
    }

    /**
     * Register commands to Multiverse's CommandHandler so we get a super sexy single menu
     */
    private void registerCommands() {
        Try.of(() -> commandManager.get())
                .andThenTry(commandManager -> serviceLocator.getAllServices(NetherPortalsCommand.class)
                        .forEach(commandManager::registerCommand))
                .onFailure(e -> {
                    Logging.severe("Failed to register commands");
                    e.printStackTrace();
                });
    }

    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        Try.run(() -> serviceLocator.getAllServices(MVNPListener.class).forEach(
                        listener -> pluginManager.registerEvents(listener, this)))
                .onFailure(e -> {
                    throw new RuntimeException("Failed to register listeners. Terminating...", e);
                });
    }

    @Override
    public void onDisable() {
        Logging.info("- Disabled");
    }

    @Override
    public String getAuthors() {
        String authors = "";
        for (int i = 0; i < this.getDescription().getAuthors().size(); i++) {
            if (i == this.getDescription().getAuthors().size() - 1) {
                authors += " and " + this.getDescription().getAuthors().get(i);
            } else {
                authors += ", " + this.getDescription().getAuthors().get(i);
            }
        }
        return authors.substring(2);
    }

    @Override
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setNetherPrefix(String netherPrefix) {
        this.MVNPConfiguration.set("portal-auto-link-when.nether.prefix", netherPrefix);
    }

    public String getNetherPrefix() {
        return this.MVNPConfiguration.getString("portal-auto-link-when.nether.prefix", DEFAULT_NETHER_PREFIX);
    }

    public void setNetherSuffix(String netherSuffix) {
        this.MVNPConfiguration.set("portal-auto-link-when.nether.suffix", netherSuffix);
    }

    public String getNetherSuffix() {
        return this.MVNPConfiguration.getString("portal-auto-link-when.nether.suffix", DEFAULT_NETHER_SUFFIX);
    }

    public void setEndPrefix(String endPrefix) {
        this.MVNPConfiguration.set("portal-auto-link-when.end.prefix", endPrefix);
    }

    public String getEndPrefix() {
        return this.MVNPConfiguration.getString("portal-auto-link-when.end.prefix", DEFAULT_END_PREFIX);
    }

    public void setEndSuffix(String endSuffix) {
        this.MVNPConfiguration.set("portal-auto-link-when.end.suffix", endSuffix);
    }

    public String getEndSuffix() {
        return this.MVNPConfiguration.getString("portal-auto-link-when.end.suffix", DEFAULT_END_SUFFIX);
    }

    public String getWorldLink(String fromWorld, PortalType type) {
        if (type == PortalType.NETHER) {
            return this.linkMap.get(fromWorld);
        } else if (type == PortalType.ENDER) {
            return this.endLinkMap.get(fromWorld);
        }

        return null;
    }

    public Map<String, String> getWorldLinks() {
        return this.linkMap;
    }

    public Map<String, String> getEndWorldLinks() {
        return this.endLinkMap;
    }

    public boolean addWorldLink(String from, String to, PortalType type) {
        if (type == PortalType.NETHER) {
            this.linkMap.put(from, to);
        } else if (type == PortalType.ENDER) {
            this.endLinkMap.put(from, to);
        } else {
            return false;
        }

        this.MVNPConfiguration.set("worlds." + from + ".portalgoesto." + type, to);
        this.saveMVNPConfig();
        return true;
    }

    public void removeWorldLink(String from, String to, PortalType type) {
        if (type == PortalType.NETHER) {
            this.linkMap.remove(from);
        } else if (type == PortalType.ENDER) {
            this.endLinkMap.remove(from);
        } else {
            return;
        }

        this.MVNPConfiguration.set("worlds." + from + ".portalgoesto." + type, null);
        this.saveMVNPConfig();
    }

    public boolean saveMVNPConfig() {
        try {
            this.MVNPConfiguration.save(new File(this.getDataFolder(), NETHER_PORTALS_CONFIG));
            return true;
        } catch (IOException e) {
            Logging.severe("Could not save " + NETHER_PORTALS_CONFIG);
        }
        return false;
    }

    public boolean isUsingBounceBack() {
        return this.MVNPConfiguration.getBoolean("bounceback", true);
    }

    public void setUsingBounceBack(boolean useBounceBack) {
        this.MVNPConfiguration.set("bounceback", useBounceBack);
    }

    public boolean isTeleportingEntities() {
        return this.MVNPConfiguration.getBoolean("teleport_entities", true);
    }

    public void setTeleportingEntities(boolean teleportingEntities) {
        this.MVNPConfiguration.set("teleport_entities", teleportingEntities);
    }

    public boolean isSendingDisabledPortalMessage() {
        return this.MVNPConfiguration.getBoolean("send_disabled_portal_message", true);
    }

    public void setSendingDisabledPortalMessage(boolean sendDisabledPortalMessage) {
        this.MVNPConfiguration.set("send_disabled_portal_message", sendDisabledPortalMessage);
    }

    public boolean isSendingNoDestinationMessage() {
        return this.MVNPConfiguration.getBoolean("send_no_destination_message", true);
    }

    public void setSendingNoDestinationMessage(boolean sendNoDestinationMessage) {
        this.MVNPConfiguration.set("send_no_destination_message", sendNoDestinationMessage);
    }

    public boolean isEndPlatformDropBlocks() {
        return this.MVNPConfiguration.getBoolean("end_platform_drop_blocks", true);
    }

    public void setEndPlatformDropBlocks(boolean endPlatformDropBlocks) {
        this.MVNPConfiguration.set("end_platform_drop_blocks", endPlatformDropBlocks);
    }

    public boolean isHandledByNetherPortals(Location l) {
        if (multiversePortals != null) {
            // Catch errors which could occur if classes aren't present or are missing methods.
            try {
                MultiversePortals portals = (MultiversePortals) multiversePortals;
                PortalManager portalManager = portals.getServiceLocator().getActiveService(PortalManager.class);
                if (portalManager != null && portalManager.isPortal(l)) {
                    return false;
                }
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Error checking if portal is handled by Multiverse-Portals", t);
            }
        }
        return true;
    }

    public void setPortals(Plugin multiversePortals) {
        this.multiversePortals = multiversePortals;
    }

    public Plugin getPortals() {
        return multiversePortals;
    }

    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    public String getDebugInfo() {
        return "[Multiverse-NetherPortals] Multiverse-NetherPortals Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-NetherPortals] Nether Prefix: " + this.getNetherPrefix() + '\n'
                + "[Multiverse-NetherPortals] Nether Suffix: " + this.getNetherSuffix() + '\n'
                + "[Multiverse-NetherPortals] End Prefix: " + this.getEndPrefix() + '\n'
                + "[Multiverse-NetherPortals] End Suffix: " + this.getEndSuffix() + '\n'
                + "[Multiverse-NetherPortals] Nether Links: " + this.getWorldLinks() + '\n'
                + "[Multiverse-NetherPortals] End Links: " + this.getEndWorldLinks() + '\n'
                + "[Multiverse-NetherPortals] Bounceback: " + this.isUsingBounceBack() + '\n'
                + "[Multiverse-NetherPortals] Teleport Entities: " + this.isTeleportingEntities() + '\n'
                + "[Multiverse-NetherPortals] Send Disabled Portal Message: " + this.isSendingDisabledPortalMessage() + '\n'
                + "[Multiverse-NetherPortals] Send No Destination Message: " + this.isSendingNoDestinationMessage() + '\n'
                + "[Multiverse-NetherPortals] Server Allow Nether: " + this.getServer().getAllowNether() + '\n'
                + "[Multiverse-NetherPortals] Server Allow End: " + this.getServer().getAllowEnd() + '\n'
                + "[Multiverse-NetherPortals] Special Code: " + "FRN001" + '\n';
    }
}
