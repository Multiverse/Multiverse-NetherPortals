package com.onarandombox.MultiverseNetherPortals.utils;

import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;

public class MVNameChecker {
	private MultiverseNetherPortals plugin;

	public MVNameChecker(MultiverseNetherPortals plugin) {
		this.plugin = plugin;
	}
	/**
	 * Returns true if the world meets the naming criteria for a nether world. It is NOT checked against the actual worlds here!
	 *
	 * @param world The world name to check
	 * @return True if the world has the correct
	 */
	public boolean isValidNetherName(String world) {
		try {
			if (world.matches("^" + this.plugin.getNetherPrefix() + ".+" + this.plugin.getNetherSuffix() + "$")) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}

    /**
     * Returns true if the world meets the naming criteria for an end world. It is NOT checked against the actual worlds here!
     *
     * @param world The world name to check
     * @return True if the world has the correct
     */
    public boolean isValidEndName(String world) {
        try {
            if (world.matches("^" + this.plugin.getEndPrefix() + ".+" + this.plugin.getEndSuffix() + "$")) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return false;
    }
	
	/**
	 * Takes a given normal name and adds the nether prefix and suffix onto it!
	 * 
	 * @param normalName
	 * @return
	 */
	public String getNetherName(String normalName) {
		return this.plugin.getNetherPrefix() + normalName + this.plugin.getNetherSuffix();
	}
	
	/**
	 * Takes a given normal name and adds the end prefix and suffix onto it!
	 * 
	 * @param normalName
	 * @return
	 */
	public String getEndName(String normalName) {
		return this.plugin.getEndPrefix() + normalName + this.plugin.getEndSuffix();
	}
	
	/**
	 * Takes a given normal name chops the suffix and prefix off!
	 *
	 * @return
	 */
	public String getNormalName(String netherName, PortalType type) {
		// Start by copying the nether name, we're going to transform it into a normal name!
		String normalName = netherName;
		// Chop off the prefix
        if (type == PortalType.NETHER) {
            if (this.plugin.getNetherPrefix().length() > 0) {
                String[] split = normalName.split(this.plugin.getNetherPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (this.plugin.getNetherSuffix().length() > 0) {
                String[] split = normalName.split(this.plugin.getNetherSuffix());
                normalName = split[0];
            }
        } else if (type == PortalType.END) {
            if (this.plugin.getNetherPrefix().length() > 0) {
                String[] split = normalName.split(this.plugin.getEndPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (this.plugin.getNetherSuffix().length() > 0) {
                String[] split = normalName.split(this.plugin.getEndSuffix());
                normalName = split[0];
            }
        }
		// All we're left with is the normal world. Don't worry if it exists, the method below will handle that!
		return normalName;
	}
}
