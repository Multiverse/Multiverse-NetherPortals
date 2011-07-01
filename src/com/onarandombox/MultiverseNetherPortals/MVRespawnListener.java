package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.onarandombox.MultiverseCore.event.MVRespawnEvent;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;

public class MVRespawnListener extends CustomEventListener {
	private MultiverseNetherPortals plugin;
	private MVNameChecker nameChecker;
	
	public MVRespawnListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
		this.nameChecker = new MVNameChecker(plugin);
	}
	
	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof MVRespawnEvent) {
			MVRespawnEvent mvevent = (MVRespawnEvent) event;
			// If the respawn type was set to "all" then WE should check to see if the
			// player is in a Nether. If they ARE, see if the corresponding real world
			// exists, if it does spawn them there
			Player p = mvevent.getPlayer();
			if (mvevent.getRespawnMethod().equalsIgnoreCase("all") && this.plugin.core.isMVWorld(p.getWorld().getName())) {
				World world = p.getWorld();
				if (world.getEnvironment() == Environment.NETHER) {
					String normalName = this.nameChecker.getNormalName(world.getName());
					if (this.plugin.core.isMVWorld(normalName)) {
						mvevent.setRespawnLocation(this.plugin.core.getServer().getWorld(normalName).getSpawnLocation());
					}
				} else {
					mvevent.setRespawnLocation(p.getWorld().getSpawnLocation());
				}
			}
		}
	}
}
