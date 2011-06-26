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
		Location currentLocation = event.getFrom();
		String currentWorld = currentLocation.getWorld().getName();
		if (currentLocation.getWorld().getEnvironment() == Environment.NETHER) {
			if (isValidNetherName(currentWorld)) {
				
				this.getNewTeleportLocation(event, currentLocation, getNormalName(currentWorld));
				
			} else {
				System.out.print("You're in a nether world, but it's not named {WORLDNAME}_nether. I'm just going to leave you here...");
				event.setCancelled(true);
			}
		} else {
			this.getNewTeleportLocation(event, currentLocation, getNetherName(currentWorld));
		}
	}
	
	/**
	 * Returns true if the world meets the naming criteria for a nether world. It is NOT checked against the actual worlds here!
	 * 
	 * @param world The world name to check
	 * @return True if the world has the correct
	 */
	private boolean isValidNetherName(String world) {
		try {
			if (world.matches("^" + this.plugin.netherPrefix + ".+" + this.plugin.netherSuffix + "$")) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}
	
	/**
	 * Takes a given normal name and adds the nether prefix and suffix onto it!
	 * @param normalName
	 * @return
	 */
	private String getNetherName(String normalName) {
		System.out.print("Getting nether name...");
		return this.plugin.netherPrefix + normalName + this.plugin.netherSuffix;
	}
	
	/**
	 * Takes a given normal name chops the suffix and prefix off!
	 * @param normalName
	 * @return
	 */
	private String getNormalName(String netherName) {
		// Start by copying the nether name, we're going to transform it into a normal name!
		String normalName = netherName;
		// Chop off the prefix
		if (this.plugin.netherPrefix.length() > 0) {
			String[] split = normalName.split(this.plugin.netherPrefix);
			normalName = split[1];
		}
		// Chop off the suffix
		if (this.plugin.netherSuffix.length() > 0) {
			String[] split = normalName.split(this.plugin.netherSuffix);
			normalName = split[0];
		}
		// All we're left with is the normal world. Don't worry if it exists, the method below will handle that!
		return normalName;
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
		double scaling = toScaling/fromScaling;
		fromLocation.setX(fromLocation.getX() * scaling);
		fromLocation.setY(fromLocation.getY() * scaling);
		return fromLocation;
	}
}
