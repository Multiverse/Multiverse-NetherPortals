package com.onarandombox.MultiverseNetherPortals.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EndPlatformCreator {

    /**
     * Creates an end platform at the specified {@code Block}
     *
     * @param world The world to create the platform in
     * @param dropEndBlocks If the platform should drop the broken blocks or delete them
     */
    public static void createEndPlatform(World world, boolean dropEndBlocks) {
        Block spawnLocation = new Location(world, 100, 49, 0).getBlock();
        Logging.fine("Creating an end platform at " + spawnLocation);

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
                        if (!dropEndBlocks || !block.breakNaturally()) {
                            block.setType(Material.AIR);
                        }
                        Logging.finest("Breaking block at " + block);
                    }
                }
            }
        }
    }

    /**
     * The default vanilla location for the end platform
     */
    public static Location getVanillaLocation(Entity entity, World world) {
        return entity instanceof Player
                ? new Location(world, 100, 49, 0, 90, 0)
                : new Location(world, 100.5, 50, 0.5, 90, 0);
    }

    /**
     * The default vanilla location for the end platform
     */
    public static Location getVanillaLocation(Entity entity, MultiverseWorld world) {
        return getVanillaLocation(entity, world.getCBWorld());
    }
}
