package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class MVNPPluginListener extends ServerListener {
	
	private MultiverseNetherPortals plugin;
	
	public MVNPPluginListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
	}
	
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
			this.plugin.core = ((MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core"));
			this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
		}
	}
}
