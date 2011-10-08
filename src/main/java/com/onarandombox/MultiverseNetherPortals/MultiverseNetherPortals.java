package com.onarandombox.MultiverseNetherPortals;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.onarandombox.MultiverseNetherPortals.commands.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.UnlinkCommand;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPConfigReloadListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPlayerListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPluginListener;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String logPrefix = "[MultiVerse-NetherPortals] ";
    private static final String NETEHR_PORTALS_CONFIG = "config.yml";
    protected static DebugLog debugLog;
    private static boolean netherDisabled;
    protected MultiverseCore core;
    protected MVNPPluginListener pluginListener;
    protected MVNPPlayerListener playerListener;
    protected MVNPConfigReloadListener customListener;
    protected Configuration MVNPconfig;
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private String netherPrefix = "";
    private String netherSuffix = DEFAULT_NETHER_SUFFIX;
    private Map<String, String> linkMap;
    protected CommandHandler commandHandler;
    private final static int requiresProtocol = 4;

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
            log.severe(logPrefix + "This version of SignPortals requires Protocol Level: " + requiresProtocol);
            log.severe(logPrefix + "Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            log.severe(logPrefix + "Grab an updated copy at: ");
            log.severe(logPrefix + "http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debugLog = new DebugLog("Multiverse-NetherPortals", getDataFolder() + File.separator + "debug.log");
        this.checkForNetherEnabled();

        this.core.incrementPluginCount();
        // As soon as we know MVCore was found, we can use the debug log!

        this.pluginListener = new MVNPPluginListener(this);
        this.playerListener = new MVNPPlayerListener(this);
        this.customListener = new MVNPConfigReloadListener(this);
        // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, this.customListener, Priority.Normal, this);

        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

        loadConfig();
        this.registerCommands();

    }

    private void checkForNetherEnabled() {
        File serverFolder = new File(this.getDataFolder().getAbsolutePath()).getParentFile().getParentFile();
        File serverProperties = new File(serverFolder.getAbsolutePath() + File.separator + "server.properties");
        try {
            FileInputStream fileStream = new FileInputStream(serverProperties);
            DataInputStream in = new DataInputStream(fileStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String propLine;
            while ((propLine = br.readLine()) != null) {
                // Print the content on the console
                if (propLine.matches("allow-nether.*") && !propLine.matches("allow-nether\\s*=\\s*true")) {
                    this.log(Level.SEVERE, "Nether is DISABLED, NetherPortals WILL NOT WORK.");
                    this.log(Level.SEVERE, "Please set 'allow-nether=true' in your server.properties file!");
                    MultiverseNetherPortals.netherDisabled = true;
                }
            }
        } catch (IOException e) {
            // This should never happen...
            this.log(Level.SEVERE, e.getMessage());
        }
    }

    public void loadConfig() {
        this.MVNPconfig = new Configuration(new File(this.getDataFolder(), NETEHR_PORTALS_CONFIG));
        this.MVNPconfig.load();
        this.linkMap = new HashMap<String, String>();

        this.setNetherPrefix(this.MVNPconfig.getString("netherportals.name.prefix", this.getNetherPrefix()));
        this.setNetherSuffix(this.MVNPconfig.getString("netherportals.name.suffix", this.getNetherSuffix()));

        if (this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
            log.warning(logPrefix + "I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
            this.setNetherSuffix(this.MVNPconfig.getString("netherportals.name.suffix", this.getNetherSuffix()));
        }

        List<String> worldKeys = this.MVNPconfig.getKeys("worlds");
        if (worldKeys != null) {
            for (String worldString : worldKeys) {
                this.linkMap.put(worldString, this.MVNPconfig.getString("worlds." + worldString + ".portalgoesto"));
            }
        }

        this.MVNPconfig.save();
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

    public String getWorldLink(String fromWorld) {
        if (this.linkMap.containsKey(fromWorld)) {
            return this.linkMap.get(fromWorld);
        }
        return null;
    }

    public Map<String, String> getWorldLinks() {
        return this.linkMap;
    }

    public void addWorldLink(String from, String to) {
        this.linkMap.put(from, to);
        this.MVNPconfig.setProperty("worlds." + from + ".portalgoesto", to);
        this.MVNPconfig.save();
    }

    public void removeWorldLink(String from, String to) {
        this.linkMap.put(from, to);
        this.MVNPconfig.removeProperty("worlds." + from);
        this.MVNPconfig.save();
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
        buffer += logAndAddToPasteBinBuffer("server.properties 'allow-nether': " + !MultiverseNetherPortals.netherDisabled);
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
