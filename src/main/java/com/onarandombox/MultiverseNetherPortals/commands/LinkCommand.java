package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class LinkCommand extends NetherPortalCommand {
    private MVWorldManager worldManager;

    public LinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Sets NP Destination");
        this.setCommandUsage("/mvnp link " + ChatColor.GOLD + "[FROM_WORLD] " + ChatColor.GREEN + " {TO_WORLD}");
        this.setArgRange(1, 2);
        this.addKey("mvnp link");
        this.addKey("mvnpl");
        this.addKey("mvnplink");
        this.setPermission("multiverse.netherportals.link", "Sets which world to link to when a player enters a NetherPortal in this world.", PermissionDefault.OP);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player) && args.size() == 1) {
            sender.sendMessage("From the command line, FROM_WORLD is required");
            sender.sendMessage("No changes were made...");
            return;
        }
        MVWorld fromWorld;
        MVWorld toWorld;
        String fromWorldString;
        String toWorldString;
        Player p;
        if (args.size() == 1) {
            p = (Player) sender;
            fromWorldString = p.getWorld().getName();
            toWorldString = args.get(0);
        } else {
            fromWorldString = args.get(0);
            toWorldString = args.get(1);
        }

        fromWorld = this.worldManager.getMVWorld(fromWorldString);
        toWorld = this.worldManager.getMVWorld(toWorldString);

        if (fromWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, fromWorldString);
            return;
        }
        if (toWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, toWorldString);
            return;
        }

        this.plugin.addWorldLink(fromWorld.getName(), toWorld.getName());
        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();
        if (fromWorld.getName().equals(toWorld.getName())) {
            sender.sendMessage(ChatColor.RED + "NOTE: " + ChatColor.WHITE + "You have successfully disabled NetherPortals in " + coloredTo);
        } else {
            sender.sendMessage("The Nether Portals in " + coloredFrom + ChatColor.WHITE + " are now linked to " + coloredTo);
        }

    }

}
