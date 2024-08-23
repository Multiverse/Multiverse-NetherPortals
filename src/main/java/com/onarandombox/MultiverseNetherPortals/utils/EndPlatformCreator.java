package com.onarandombox.MultiverseNetherPortals.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class EndPlatformCreator {

    /**
     * Creates an end platform at the specified {@code Block}
     *
     * @param spawnLocation The {@code Block} that the player will spawn at.
     * @param dropEndBlocks If the platform should drop the broken blocks or delete them
     */
    public static void createEndPlatform(Block spawnLocation, boolean dropEndBlocks) {
        Logging.fine("Creating an end platform at " + spawnLocation.toString());

        for (int x = spawnLocation.getX() - 2; x <= spawnLocation.getX() + 2; x++) {
            for (int z = spawnLocation.getZ() - 2; z <= spawnLocation.getZ() + 2; z++) {
                Block platformBlock = spawnLocation.getWorld().getBlockAt(x, spawnLocation.getY() - 1, z);
                Logging.finest("Placing blocks at " + platformBlock);

                // Create platform
                if (platformBlock.getType() != Material.OBSIDIAN) {
                    platformBlock.setType(Material.OBSIDIAN);
                    Logging.finest("Placing obsidian at " + platformBlock);
                }

                // Clear space above platform
                for (int yMod = 1; yMod <= 3; yMod++) {
                    Block block = platformBlock.getRelative(BlockFace.UP, yMod);
                    if (block.getType() != Material.AIR) {
                        if (dropEndBlocks) {
                            block.breakNaturally();
                        } else {
                            block.setType(Material.AIR);
                        }

                        Logging.finest("Breaking block at " + platformBlock);
                    }
                }
            }
        }
    }


    /**
     * Creates an end platform at the specified {@code Location}
     * @param spawnLocation The {@code Location} that the player will spawn at.
     */
    public static void createEndPlatform(Location spawnLocation, boolean dropEndBlocks) {
        createEndPlatform(spawnLocation.getBlock(), dropEndBlocks);
    }

    /**
     * The default vanilla location for the end platform
     */
    public static Location getVanillaLocation(World world) {
        return new Location(world, 100, 49, 0, 90, 0);
    }

    /**
     * The default vanilla location for the end platform
     */
    public static Location getVanillaLocation(MultiverseWorld world) {
        return getVanillaLocation(world.getCBWorld());
    }
}
