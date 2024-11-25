package org.mvplugins.multiverse.netherportals.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;

@Service
public class MVNameChecker {

    private final MultiverseNetherPortals plugin;
    private final WorldManager worldManager;

    @Inject
    MVNameChecker(@NotNull MultiverseNetherPortals plugin, @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
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
        final String netherName = this.plugin.getNetherPrefix() + normalName + this.plugin.getNetherSuffix();
        if (worldManager.isLoadedWorld(netherName)) {
            Logging.finest("Selected nether world '" + netherName + "' for normal '" + normalName + "'");
        }
        return netherName;
    }

    /**
     * Takes a given normal name and adds the end prefix and suffix onto it!
     *
     * @param normalName
     * @return
     */
    public String getEndName(String normalName) {
        final String endName = this.plugin.getEndPrefix() + normalName + this.plugin.getEndSuffix();
        if (worldManager.isLoadedWorld(endName)) {
            Logging.finest("Selected end world '" + endName + "' for normal '" + normalName + "'");
        }
        return endName;
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
            if (!this.plugin.getNetherPrefix().isEmpty()) {
                String[] split = normalName.split(this.plugin.getNetherPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (!this.plugin.getNetherSuffix().isEmpty()) {
                String[] split = normalName.split(this.plugin.getNetherSuffix());
                normalName = split[0];
            }
        } else if (type == PortalType.ENDER) {
            if (!this.plugin.getNetherPrefix().isEmpty()) {
                String[] split = normalName.split(this.plugin.getEndPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (!this.plugin.getNetherSuffix().isEmpty()) {
                String[] split = normalName.split(this.plugin.getEndSuffix());
                normalName = split[0];
            }
        }
        if (!normalName.equals(netherName) && worldManager.isLoadedWorld(normalName)) {
            Logging.finest("Selected normal world '" + normalName + "' for " + type + " '" + netherName + "'");
        }
		// All we're left with is the normal world. Don't worry if it exists, the method below will handle that!
        return normalName;
    }
}
