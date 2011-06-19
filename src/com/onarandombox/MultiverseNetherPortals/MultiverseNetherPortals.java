package com.onarandombox.MultiverseNetherPortals;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.utils.DebugLog;

public class MultiverseNetherPortals extends JavaPlugin {
	
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String logPrefix = "[MultiVerse-NetherPortals] ";
	protected static DebugLog debugLog;
	protected MultiverseCore core;
	protected MVNPPluginListener pluginListener;
	
	
	@Override
	public void onEnable() {
		pluginListener = new MVNPPluginListener(this);
	    // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initially.
        getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Normal, this);
        
		log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
	}
	
	@Override
	public void onDisable() {
		log.info(logPrefix + "- Disabled");
	}
	
	@Override
	public void onLoad() {
		getDataFolder().mkdirs();
		debugLog = new DebugLog("Multiverse-NetherPortals", getDataFolder() + File.separator + "debug.log");
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
}
