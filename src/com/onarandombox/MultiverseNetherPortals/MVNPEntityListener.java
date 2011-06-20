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
		String worldFrom = event.getLocation().getWorld().getName();
		
		//System.out.print(worldFrom.substring(worldFrom.length() - 7, worldFrom.length()));
		if(worldFrom.length() > 7 && worldFrom.substring(worldFrom.length() - 7, worldFrom.length()).equalsIgnoreCase("_nether")) {
			//System.out.print("Woo Free candy!!!");
		}
//		if (plugin.core.ph.canEnterWorld(event.getPlayer(), plugin.getServer().getWorld(d.getName()))) {
//            Location l = playerTeleporter.getSafeDestination(this.plugin.getServer().getWorld(d.getName()).getSpawnLocation());
//            p.teleport(l);
//		}
		super.onEntityPortalEnter(event);
	}
}
