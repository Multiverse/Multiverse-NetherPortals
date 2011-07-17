package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;

public class LinkCommand extends NetherPortalCommand {

    public LinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Sets NP Destination");
        this.setCommandUsage("/mvnp link " + ChatColor.GOLD + "[FROM_WORLD] " + ChatColor.GREEN + " {TO_WORLD}");
        this.setArgRange(1, 2);
        this.addKey("mvnp link");
        this.addKey("mvnpl");
        this.addKey("mvnplink");
        this.setPermission("multiverse.netherportals.link", "Sets which world to link to when a player enters a NetherPortal in this world.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player) && args.size() == 1) {
            sender.sendMessage("From the command line, FROM_WORLD is required");
            sender.sendMessage("No changes were made...");
            return;
        }
        MVWorld fromWorld = null;
        MVWorld toWorld = null;
        String fromWorldString = null;
        String toWorldString = null;
        Player p = null;
        if (args.size() == 1) {
            p = (Player) sender;
            fromWorldString = p.getWorld().getName();
            toWorldString = args.get(0);
        } else {
            fromWorldString = args.get(0);
            toWorldString = args.get(1);
        }

        fromWorld = this.plugin.getCore().getMVWorld(fromWorldString);
        toWorld = this.plugin.getCore().getMVWorld(toWorldString);
        
        if(fromWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, fromWorldString);
            return;
        }
        if(toWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, toWorldString);
            return;
        }

        if (fromWorld.getName().equals(toWorld.getName())) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " Looks like you tried to link portals in the same world!");
            return;
        }
        ((MultiverseNetherPortals) this.plugin).addWorldLink(fromWorld.getName(), toWorld.getName());
        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();
        sender.sendMessage("The Nether Portals in " + coloredFrom + ChatColor.WHITE + " are now linked to " + coloredTo + ChatColor.WHITE + ".");

    }

}
