package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;


public class MVNPPlayerListener extends PlayerListener {
	
	private MultiverseNetherPortals plugin;
	
	public MVNPPlayerListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerPortal(PlayerPortalEvent event) {
		Location fromLocation = event.getFrom();
		String fromWorldString = fromLocation.getWorld().getName();
		// TODO: Allow custom naming
		final int netherNameLength = 7;
		if (fromLocation.getWorld().getEnvironment() == Environment.NETHER) {
			if(fromWorldString.length() > netherNameLength && fromWorldString.substring(fromWorldString.length() - netherNameLength, fromWorldString.length()).equalsIgnoreCase("_nether")) {
				
				String worldstring = fromWorldString.substring(0,fromWorldString.length() - netherNameLength);
				
				
				getNewTeleportLocation(event, fromLocation, worldstring);
				
			} else {
				System.out.print("You're in a nether world, but it's not named {WORLDNAME}_nether. I'm just going to leave you here...");
				event.setCancelled(true);
			}
		} else {
			getNewTeleportLocation(event, fromLocation, fromWorldString + "_nether");
		}
	}

	private void getNewTeleportLocation(PlayerPortalEvent event, Location fromLocation, String worldstring) {
		World tpto = this.plugin.getServer().getWorld(worldstring); 
		if(tpto != null) {
			// Set the output location to the same XYZ coords but different world
			// TODO: Add scaling
			fromLocation.setWorld(tpto);
			event.setTo(fromLocation);
			System.out.print("I will try to take you to: " + worldstring);
		} else {
			System.out.print("Looks like " + worldstring + " does not exist. Whoops on your part!");
			// Set the event to redirect back to the same portal
			// otherwise they sit in the jelly stuff forever!
			event.setTo(fromLocation);
		}
	}
}
