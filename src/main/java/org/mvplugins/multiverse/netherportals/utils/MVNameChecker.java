package org.mvplugins.multiverse.netherportals.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

@Service
public class MVNameChecker {

    private final NetherPortalsConfig config;
    private final WorldManager worldManager;

    @Inject
    MVNameChecker(@NotNull NetherPortalsConfig config, @NotNull WorldManager worldManager) {
        this.config = config;
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
            if (world.matches("^" + this.config.getNetherPrefix() + ".+" + this.config.getNetherSuffix() + "$")) {
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
            if (world.matches("^" + this.config.getEndPrefix() + ".+" + this.config.getEndSuffix() + "$")) {
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
        final String netherName = this.config.getNetherPrefix() + normalName + this.config.getNetherSuffix();
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
        final String endName = this.config.getEndPrefix() + normalName + this.config.getEndSuffix();
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
            if (!this.config.getNetherPrefix().isEmpty()) {
                String[] split = normalName.split(this.config.getNetherPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (!this.config.getNetherSuffix().isEmpty()) {
                String[] split = normalName.split(this.config.getNetherSuffix());
                normalName = split[0];
            }
        } else if (type == PortalType.ENDER) {
            if (!this.config.getEndPrefix().isEmpty()) {
                String[] split = normalName.split(this.config.getEndPrefix());
                normalName = split[1];
            }
            // Chop off the suffix
            if (!this.config.getEndSuffix().isEmpty()) {
                String[] split = normalName.split(this.config.getEndSuffix());
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
