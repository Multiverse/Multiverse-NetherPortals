package com.onarandombox.MultiverseNetherPortals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;

public class MVNPPlayerListener extends PlayerListener {
	
	private MultiverseNetherPortals plugin;
	private MVNameChecker nameChecker;
	
	public MVNPPlayerListener(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
		this.nameChecker = new MVNameChecker(plugin);
	}
	
	@Override
	public void onPlayerPortal(PlayerPortalEvent event) {
		Location currentLocation = event.getFrom();
		String currentWorld = currentLocation.getWorld().getName();
		if (this.nameChecker.isValidNetherName(currentWorld)) {
			this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNormalName(currentWorld));
		} else {
			this.getNewTeleportLocation(event, currentLocation, this.nameChecker.getNetherName(currentWorld));
		}
	}
	

	
	private void getNewTeleportLocation(PlayerPortalEvent event, Location fromLocation, String worldstring) {
		World tpto = this.plugin.getServer().getWorld(worldstring);
		if (tpto != null && this.plugin.core.ph.canEnterWorld(event.getPlayer(), tpto)) {
			// Set the output location to the same XYZ coords but different world
			// TODO: Add scaling
			double toScaling = this.plugin.core.getMVWorld(tpto.getName()).getScaling();
			double fromScaling = this.plugin.core.getMVWorld(event.getFrom().getWorld().getName()).getScaling();
			
			fromLocation = this.getScaledLocation(fromLocation, fromScaling, toScaling);
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
	
	private Location getScaledLocation(Location fromLocation, double fromScaling, double toScaling) {
		double scaling = toScaling / fromScaling;
		fromLocation.setX(fromLocation.getX() * scaling);
		fromLocation.setZ(fromLocation.getZ() * scaling);
		return fromLocation;
	}
}
