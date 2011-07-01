package com.onarandombox.MultiverseNetherPortals;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.utils.DebugLog;

public class MultiverseNetherPortals extends JavaPlugin {
	
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String logPrefix = "[MultiVerse-NetherPortals] ";
	private static final String NETEHR_PORTALS_CONFIG = "config.yml";
	protected static DebugLog debugLog;
	protected MultiverseCore core;
	protected MVNPPluginListener pluginListener;
	protected MVNPPlayerListener playerListener;
	protected MVRespawnListener respawnListener;
	protected Configuration MVNPconfig;
	private static final String DEFAULT_NETHER_SUFFIX = "_nether";
	private String netherPrefix = "";
	private String netherSuffix = DEFAULT_NETHER_SUFFIX;
	
	@Override
	public void onEnable() {
		this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

	    // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            log.info(logPrefix + "Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // As soon as we know MVCore was found, we can use the debug log!
        debugLog = new DebugLog("Multiverse-NetherPortals", getDataFolder() + File.separator + "debug.log");
		this.pluginListener = new MVNPPluginListener(this);
		this.playerListener = new MVNPPlayerListener(this);
		this.respawnListener = new MVRespawnListener(this);
		// Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
		this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this.pluginListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, this.playerListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, this.respawnListener, Priority.Normal, this);
		
		log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
		
		
		loadConfig();
	}
	
	private void loadConfig() {
		this.MVNPconfig = new Configuration(new File(this.getDataFolder(), NETEHR_PORTALS_CONFIG));
		this.MVNPconfig.load();
		
		this.setNetherPrefix(this.MVNPconfig.getString("netherportals.name.prefix", this.getNetherPrefix()));
		this.setNetherSuffix(this.MVNPconfig.getString("netherportals.name.suffix", this.getNetherSuffix()));
		
		if(this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
			log.warning(logPrefix + "I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
			this.setNetherSuffix(this.MVNPconfig.getString("netherportals.name.suffix", this.getNetherSuffix()));
		}
		
		this.MVNPconfig.save();
	}

	@Override
	public void onDisable() {
		log.info(logPrefix + "- Disabled");
	}
	
	@Override
	public void onLoad() {
		getDataFolder().mkdirs();
	}
	
	/**
	 * Parse the Authors Array into a readable String with ',' and 'and'.
	 * 
	 * @return
	 */
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
		return netherPrefix;
	}

	public void setNetherSuffix(String netherSuffix) {
		this.netherSuffix = netherSuffix;
	}

	public String getNetherSuffix() {
		return netherSuffix;
	}
}
