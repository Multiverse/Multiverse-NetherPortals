package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.server.PluginEnableEvent;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.DestinationType;

public class MVNPPlayerListener extends PlayerListener {
	
	private MultiverseNetherPortals plugin;
	private MVTeleport playerTeleporter;
	
	public MVNPPlayerListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
		playerTeleporter = plugin.core.getTeleporter();
	}
	@Override
	public void onPlayerPortal(PlayerPortalEvent event) {

	}
}
