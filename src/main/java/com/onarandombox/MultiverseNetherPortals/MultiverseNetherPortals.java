package com.onarandombox.MultiverseNetherPortals;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseNetherPortals.commands.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.UnlinkCommand;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPCoreListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPEntityListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPlayerListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPluginListener;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final String NETEHR_PORTALS_CONFIG = "config.yml";
    protected MultiverseCore core;
    protected MVNPPluginListener pluginListener;
    protected MVNPPlayerListener playerListener;
    protected MVNPCoreListener customListener;
    protected FileConfiguration MVNPconfiguration;
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private static final String DEFAULT_END_SUFFIX = "_the_end";
    private String netherPrefix = "";
    private String netherSuffix = DEFAULT_NETHER_SUFFIX;
    private String endPrefix = "";
    private String endSuffix = DEFAULT_END_SUFFIX;
    private Map<String, String> linkMap;
    private Map<String, String> endLinkMap;
    protected CommandHandler commandHandler;
    private final static int requiresProtocol = 9;
    private MVNPEntityListener entityListener;

    @Override
    public void onEnable() {
        Logging.init(this);
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

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
            Logging.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.core.incrementPluginCount();
        // As soon as we know MVCore was found, we can use the debug log!

        this.pluginListener = new MVNPPluginListener(this);
        this.playerListener = new MVNPPlayerListener(this);
        this.entityListener = new MVNPEntityListener(this);
        this.customListener = new MVNPCoreListener(this);
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this.pluginListener, this);
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        pm.registerEvents(this.customListener, this);

        Logging.info("- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

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

        this.setUsingBounceBack(this.isUsingBounceBack());

        this.setNetherPrefix(this.MVNPconfiguration.getString("netherportals.name.prefix", this.getNetherPrefix()));
        this.setNetherSuffix(this.MVNPconfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));

        if (this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
            Logging.warning("I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
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
        for (com.pneumaticraft.commandhandler.multiverse.Command c : this.commandHandler.getAllCommands()) {
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
        Logging.info("- Disabled");
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

    public String getEndPrefix() {
        return this.endPrefix;
    }

    public String getEndSuffix() {
        return this.endSuffix;
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

    public boolean isUsingBounceBack() {
        return this.MVNPconfiguration.getBoolean("bounceback", true);
    }

    public void setUsingBounceBack(boolean useBounceBack) {
        this.MVNPconfiguration.set("bounceback", useBounceBack);
    }

    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    @Override
    public void log(Level level, String msg) {
        Logging.log(level, msg);
    }

    @Override
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

    public String getVersionInfo() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[Multiverse-NetherPortals] Multiverse-NetherPortals Version: ").append(this.getDescription().getVersion()).append('\n');
        buffer.append("[Multiverse-NetherPortals] World links: ").append(this.getWorldLinks()).append('\n');
        buffer.append("[Multiverse-NetherPortals] Nether Prefix: ").append(netherPrefix).append('\n');
        buffer.append("[Multiverse-NetherPortals] Nether Suffix: ").append(netherSuffix).append('\n');
        buffer.append("[Multiverse-NetherPortals] Special Code: ").append("FRN001").append('\n');
        return buffer.toString();
    }
}
