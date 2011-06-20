package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

public class MVNPEntityListener extends EntityListener {
	
	private MultiverseNetherPortals plugin;
	
	public MVNPEntityListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
	}
	@Override
	public void onEntityPortalEnter(EntityPortalEnterEvent event) {
		// This file isn't actually used. I may use it though...
		super.onEntityPortalEnter(event);
	}
}
