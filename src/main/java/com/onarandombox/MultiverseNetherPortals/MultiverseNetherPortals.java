package com.onarandombox.MultiverseNetherPortals;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.onarandombox.MultiverseNetherPortals.commands.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.UnlinkCommand;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPConfigReloadListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPEntityListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPlayerListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPluginListener;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String logPrefix = "[MultiVerse-NetherPortals] ";
    private static final String NETEHR_PORTALS_CONFIG = "config.yml";
    protected static DebugLog debugLog;
    protected MultiverseCore core;
    protected MVNPPluginListener pluginListener;
    protected MVNPPlayerListener playerListener;
    protected MVNPConfigReloadListener customListener;
    protected FileConfiguration MVNPconfiguration;
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private String netherPrefix = "";
    private String netherSuffix = DEFAULT_NETHER_SUFFIX;
    private Map<String, String> linkMap;
    private Map<String, String> endLinkMap;
    protected CommandHandler commandHandler;
    private final static int requiresProtocol = 9;
    private MVNPEntityListener entityListener;

    @Override
    public void onEnable() {
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            log.info(logPrefix + "Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            log.severe(logPrefix + "Your Multiverse-Core is OUT OF DATE");
            log.severe(logPrefix + "This version of NetherPortals requires Protocol Level: " + requiresProtocol);
            log.severe(logPrefix + "Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            log.severe(logPrefix + "Grab an updated copy at: ");
            log.severe(logPrefix + "http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debugLog = new DebugLog("Multiverse-NetherPortals", getDataFolder() + File.separator + "debug.log");

        this.core.incrementPluginCount();
        // As soon as we know MVCore was found, we can use the debug log!

        this.pluginListener = new MVNPPluginListener(this);
        this.playerListener = new MVNPPlayerListener(this);
        this.entityListener = new MVNPEntityListener(this);
        this.customListener = new MVNPConfigReloadListener(this);
        // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, this.customListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.ENTITY_PORTAL_ENTER, this.entityListener, Priority.Monitor, this);

        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

        loadConfig();
        this.registerCommands();

    }

    public void loadConfig() {
        this.MVNPconfiguration = new YamlConfiguration();
        try {
            this.MVNPconfiguration.load(new File(this.getDataFolder(), NETEHR_PORTALS_CONFIG));
        } catch (IOException e) {
            this.log(Level.SEVERE, "Could not load " + NETEHR_PORTALS_CONFIG);
        } catch (InvalidConfigurationException e) {
            this.log(Level.SEVERE, NETEHR_PORTALS_CONFIG + " contained INVALID YAML. Please look at the file.");
        }
        this.linkMap = new HashMap<String, String>();
        this.endLinkMap = new HashMap<String, String>();

        this.setNetherPrefix(this.MVNPconfiguration.getString("netherportals.name.prefix", this.getNetherPrefix()));
        this.setNetherSuffix(this.MVNPconfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));

        if (this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
            log.warning(logPrefix + "I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
            this.setNetherSuffix(this.MVNPconfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));
        }
        if (this.MVNPconfiguration.getConfigurationSection("worlds") == null) {
            this.MVNPconfiguration.createSection("worlds");
        }
        Set<String> worldKeys = this.MVNPconfiguration.getConfigurationSection("worlds").getKeys(false);
        if (worldKeys != null) {
            for (String worldString : worldKeys) {
                String nether = this.MVNPconfiguration.getString("worlds." + worldString + ".portalgoesto.NETHER", null);
                String ender = this.MVNPconfiguration.getString("worlds." + worldString + ".portalgoesto.END", null);
                if (nether != null) {
                    this.linkMap.put(worldString, nether);
                }
                if (ender != null) {
                    this.endLinkMap.put(worldString, ender);
                }

            }
        }
        this.saveMVNPConfig();
    }

    /** Register commands to Multiverse's CommandHandler so we get a super sexy single menu */
    private void registerCommands() {
        this.commandHandler = this.core.getCommandHandler();
        this.commandHandler.registerCommand(new LinkCommand(this));
        this.commandHandler.registerCommand(new UnlinkCommand(this));
        this.commandHandler.registerCommand(new ShowLinkCommand(this));
        for (com.pneumaticraft.commandhandler.Command c : this.commandHandler.getAllCommands()) {
            if (c instanceof HelpCommand) {
                c.addKey("mvnp");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!this.isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        ArrayList<String> allArgs = new ArrayList<String>(Arrays.asList(args));
        allArgs.add(0, command.getName());
        return this.commandHandler.locateAndRunCommand(sender, allArgs);
    }

    @Override
    public void onDisable() {
        log.info(logPrefix + "- Disabled");
    }

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();
    }

    private String getAuthors() {
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

    public void setNetherPrefix(String netherPrefix) {
        this.netherPrefix = netherPrefix;
    }

    public String getNetherPrefix() {
        return this.netherPrefix;
    }

    public void setNetherSuffix(String netherSuffix) {
        this.netherSuffix = netherSuffix;
    }

    public String getNetherSuffix() {
        return this.netherSuffix;
    }

    public String getWorldLink(String fromWorld, PortalType type) {
        if (type == PortalType.NETHER) {
            return this.linkMap.get(fromWorld);
        } else if (type == PortalType.END) {
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
        } else if (type == PortalType.END) {
            this.endLinkMap.put(from, to);
        } else {
            return false;
        }

        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type, to);
        this.saveMVNPConfig();
        return true;
    }

    public void removeWorldLink(String from, String to, PortalType type) {
        if (type == PortalType.NETHER) {
            this.linkMap.remove(from);
        } else if (type == PortalType.END) {
            this.endLinkMap.remove(from);
        } else {
            return;
        }

        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type, null);
        this.saveMVNPConfig();
    }

    public boolean saveMVNPConfig() {
        try {
            this.MVNPconfiguration.save(new File(this.getDataFolder(), NETEHR_PORTALS_CONFIG));
            return true;
        } catch (IOException e) {
            this.log(Level.SEVERE, "Could not save " + NETEHR_PORTALS_CONFIG);
        }
        return false;
    }

    public MultiverseCore getCore() {
        return this.core;
    }

    public void log(Level level, String msg) {
        log.log(level, logPrefix + " " + msg);
        debugLog.log(level, logPrefix + " " + msg);
    }

    public void setCore(MultiverseCore core) {
        this.core = core;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-NetherPortals Version: " + this.getDescription().getVersion());
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("World links: " + this.getWorldLinks());
        buffer += logAndAddToPasteBinBuffer("Nether Prefix: " + netherPrefix);
        buffer += logAndAddToPasteBinBuffer("Nether Suffix: " + netherSuffix);
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-NetherPortals] " + string + "\n";
    }
}
