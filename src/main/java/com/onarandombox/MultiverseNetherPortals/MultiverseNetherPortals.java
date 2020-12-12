package com.onarandombox.MultiverseNetherPortals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseNetherPortals.commands.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.UnlinkCommand;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPCoreListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPEntityListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPlayerListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPluginListener;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.commandhandler.CommandHandler;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final String NETHER_PORTALS_CONFIG = "config.yml";
    protected MultiverseCore core;
    protected Plugin multiversePortals;
    protected MVNPPluginListener pluginListener;
    protected MVNPPlayerListener playerListener;
    protected MVNPCoreListener customListener;
    protected FileConfiguration MVNPConfiguration;
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private static final String DEFAULT_END_SUFFIX = "_the_end";
    private String netherPrefix = "";
    private String netherSuffix = DEFAULT_NETHER_SUFFIX;
    private String endPrefix = "";
    private String endSuffix = DEFAULT_END_SUFFIX;
    private Map<String, String> linkMap;
    private Map<String, String> endLinkMap;
    protected CommandHandler commandHandler;
    private final static int requiresProtocol = 24;
    private MVNPEntityListener entityListener;

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

        Logging.setDebugLevel(core.getMVConfig().getGlobalDebug());

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

        loadConfig();
        this.registerCommands();

        Logging.log(true, Level.INFO, " Enabled - By %s", getAuthors());
    }

    public void loadConfig() {
        initMVNPConfig();

        this.linkMap = new HashMap<String, String>();
        this.endLinkMap = new HashMap<String, String>();

        this.setUsingBounceBack(this.isUsingBounceBack());
        this.setTeleportingEntities(this.isTeleportingEntities());
        this.setSendingNoDestinationMessage(this.isSendingNoDestinationMessage());
        this.setSendingDisabledPortalMessage(this.isSendingDisabledPortalMessage());
        this.setNetherPrefix(this.MVNPConfiguration.getString("netherportals.name.prefix", this.getNetherPrefix()));
        this.setNetherSuffix(this.MVNPConfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));

        if (this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
            Logging.warning("I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
            this.setNetherSuffix(this.MVNPConfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));
        }
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
            this.log(Level.SEVERE, "Could not load " + NETHER_PORTALS_CONFIG);
        }
        catch (InvalidConfigurationException e) {
            this.log(Level.SEVERE, NETHER_PORTALS_CONFIG + " contained INVALID YAML. Please look at the file.");
        }
    }

    /** Register commands to Multiverse's CommandHandler so we get a super sexy single menu */
    private void registerCommands() {
        this.commandHandler = this.core.getCommandHandler();
        this.commandHandler.registerCommand(new LinkCommand(this));
        this.commandHandler.registerCommand(new UnlinkCommand(this));
        this.commandHandler.registerCommand(new ShowLinkCommand(this));
        for (com.onarandombox.commandhandler.Command c : this.commandHandler.getAllCommands()) {
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
            this.log(Level.SEVERE, "Could not save " + NETHER_PORTALS_CONFIG);
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

    public boolean isHandledByNetherPortals(Location l) {
        if (multiversePortals != null) {
            // Catch errors which could occur if classes aren't present or are missing methods.
            try {
                MultiversePortals portals = (MultiversePortals) multiversePortals;
                if (portals.getPortalManager().isPortal(l)) {
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
        buffer += logAndAddToPasteBinBuffer("Nether Prefix: " + this.getNetherPrefix());
        buffer += logAndAddToPasteBinBuffer("Nether Suffix: " + this.getNetherSuffix());
        buffer += logAndAddToPasteBinBuffer("End Prefix: " + this.getEndPrefix());
        buffer += logAndAddToPasteBinBuffer("End Suffix: " + this.getEndSuffix());
        buffer += logAndAddToPasteBinBuffer("Nether Links: " + this.getWorldLinks());
        buffer += logAndAddToPasteBinBuffer("End Links: " + this.getEndWorldLinks());
        buffer += logAndAddToPasteBinBuffer("Bounceback: " + this.isUsingBounceBack());
        buffer += logAndAddToPasteBinBuffer("Teleport Entities: " + this.isTeleportingEntities());
        buffer += logAndAddToPasteBinBuffer("Send Disabled Portal Message: " + this.isSendingDisabledPortalMessage());
        buffer += logAndAddToPasteBinBuffer("Send No Destination Message: " + this.isSendingNoDestinationMessage());
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-NetherPortals] " + string + '\n';
    }

    public String getVersionInfo() {
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
