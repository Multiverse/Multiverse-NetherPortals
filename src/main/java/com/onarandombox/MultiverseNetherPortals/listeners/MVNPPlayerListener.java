package com.onarandombox.MultiverseNetherPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.utils.MVLinkChecker;
import com.onarandombox.MultiverseNetherPortals.utils.MVNameChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MVNPPlayerListener implements Listener {

    private final MultiverseNetherPortals plugin;
    private final MVNameChecker nameChecker;
    private final MVLinkChecker linkChecker;
    private final MVWorldManager worldManager;
    private final PermissionTools pt;
    private final Advancement enterNetherAdvancement;
    private final Advancement enterEndAdvancement;

    private static final String ENTER_NETHER_CRITERIA = "entered_nether";
    private static final String ENTER_END_CRITERIA = "entered_end";

    public MVNPPlayerListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.nameChecker = this.plugin.getNameChecker();
        this.worldManager = this.plugin.getCore().getMVWorldManager();
        this.pt = new PermissionTools(this.plugin.getCore());
        this.linkChecker = this.plugin.getLinkChecker();
        this.enterNetherAdvancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft("story/enter_the_nether"));
        this.enterEndAdvancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft("story/enter_the_end"));
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            Logging.finest("PlayerPortalEvent was cancelled! NOT teleporting!");
            return;
        }

        Location currentLocation = event.getFrom().clone();
        if (!plugin.isHandledByNetherPortals(currentLocation)) {
            return;
        }

        PortalType type;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            type = PortalType.ENDER;
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            type = PortalType.NETHER;
        } else {
            return;
        }

        if (type == PortalType.NETHER) {
            try {
                Class.forName("org.bukkit.TravelAgent");
                event.useTravelAgent(true);
            } catch (ClassNotFoundException ignore) {
                Logging.fine("TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName());
            }
        }

        Location newTo;
        String currentWorld = currentLocation.getWorld().getName();
        String linkedWorld = this.plugin.getWorldLink(currentWorld, type);
        if (currentWorld.equalsIgnoreCase(linkedWorld)) {
            newTo = null;
        } else if (linkedWorld != null) {
            newTo = this.linkChecker.findNewTeleportLocation(currentLocation, linkedWorld, event.getPlayer());
        } else if (this.nameChecker.isValidNetherName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.NETHER), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(this.nameChecker.getNormalName(currentWorld, PortalType.NETHER)), event.getPlayer());
            }
        } else if (this.nameChecker.isValidEndName(currentWorld)) {
            if (type == PortalType.NETHER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(this.nameChecker.getNormalName(currentWorld, PortalType.ENDER)), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNormalName(currentWorld, PortalType.ENDER), event.getPlayer());
            }
        } else {
            if (type == PortalType.ENDER) {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getEndName(currentWorld), event.getPlayer());
            } else {
                newTo = this.linkChecker.findNewTeleportLocation(currentLocation, this.nameChecker.getNetherName(currentWorld), event.getPlayer());
            }
        }

        if (newTo != null) {
            event.setTo(newTo);
        } else {
            event.setCancelled(true);
            return;
        }

        MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
        MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());

        if (!event.isCancelled()) {
            if (fromWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
                Logging.fine("Player '" + event.getPlayer().getName() + "' will be teleported to the spawn of '" + toWorld.getName() + "' since they used an end exit portal.");
                try {
                    Class.forName("org.bukkit.TravelAgent");
                    event.getPortalTravelAgent().setCanCreatePortal(false);
                } catch (ClassNotFoundException ignore) {
                    Logging.fine("TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName() + ". There may be a portal created at spawn.");
                }
                if (toWorld.getBedRespawn()
                        && event.getPlayer().getBedSpawnLocation() != null
                        && event.getPlayer().getBedSpawnLocation().getWorld().getUID() == toWorld.getCBWorld().getUID()) {
                    event.setTo(event.getPlayer().getBedSpawnLocation());
                } else {
                    event.setTo(toWorld.getSpawnLocation());
                }
            } else if (fromWorld.getEnvironment() == World.Environment.NETHER && type == PortalType.NETHER) {
                try {
                    Class.forName("org.bukkit.TravelAgent");
                    event.getPortalTravelAgent().setCanCreatePortal(true);
                    event.setTo(event.getPortalTravelAgent().findOrCreate(event.getTo()));
                } catch (ClassNotFoundException ignore) {
                    Logging.fine("TravelAgent not available for PlayerPortalEvent for " + event.getPlayer().getName() + ". Their destination may not be correct.");
                    event.setTo(event.getTo());
                }
            } else if (toWorld.getEnvironment() == World.Environment.THE_END && type == PortalType.ENDER) {
                Location loc = new Location(event.getTo().getWorld(), 100, 50, 0); // This is the vanilla location for obsidian platform.
                event.setTo(loc);
                Block block = loc.getBlock();
                for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                    for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                        Block platformBlock = loc.getWorld().getBlockAt(x, block.getY() - 1, z);
                        if (platformBlock.getType() != Material.OBSIDIAN) {
                            platformBlock.setType(Material.OBSIDIAN);
                        }
                        for (int yMod = 1; yMod <= 3; yMod++) {
                            Block b = platformBlock.getRelative(BlockFace.UP, yMod);
                            if (b.getType() != Material.AIR) {
                                b.setType(Material.AIR);
                            }
                        }
                    }
                }
            }

            // Advancements need to be triggered manually
            if (type == PortalType.NETHER && event.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
                awardAdvancement(event.getPlayer(), enterNetherAdvancement, ENTER_NETHER_CRITERIA);
            } else if (type == PortalType.ENDER && event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                awardAdvancement(event.getPlayer(), enterEndAdvancement, ENTER_END_CRITERIA);
            }
        }
    }

    /**
     * Award an advancement criteria to a player if not already awarded.
     *
     * @param player        Target player to award the advancement criteria to.
     * @param advancement   {@link Advancement} the criteria belongs to.
     * @param criteria      Criteria to award the player.
     */
    private void awardAdvancement(Player player, Advancement advancement, String criteria) {
        if (advancement == null) {
            Logging.fine("No advancement found for target criteria: %s", criteria);
            return;
        }
        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
        if (advancementProgress.isDone()) {
            Logging.fine("%s has already been awarded advancement criteria %s.", player.getName(), criteria);
            return;
        }
        if (!advancementProgress.awardCriteria(criteria)) {
            Logging.warning("Unable to award advancement criteria %s to %s.", criteria, player.getName());
            return;
        }
        Logging.fine("Awarded advancement criteria %s to %s.", criteria, player.getName());
    }
}
