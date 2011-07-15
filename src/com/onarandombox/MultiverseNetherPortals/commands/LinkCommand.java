package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.pneumaticraft.commandhandler.Command;

public class LinkCommand extends Command {

    public LinkCommand(JavaPlugin plugin) {
        super(plugin);
        this.commandName = "Sets NP Destination";
        this.commandDesc = "Sets which world to link to when a player enters a NetherPortal in this world.";
        this.commandUsage = "/mvnp link " + ChatColor.GOLD + "[FROM_WORLD] " + ChatColor.GREEN + " {TO_WORLD}";
        this.minimumArgLength = 1;
        this.maximumArgLength = 2;
        this.commandKeys.add("mvnp link");
        this.commandKeys.add("mvnpl");
        this.commandKeys.add("mvnplink");
        this.permission = "multiverse.netherportals.link";
        this.opRequired = true;
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

        fromWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(fromWorldString);
        toWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(toWorldString);
        
        if(fromWorld == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " Multiverse doesn't know about: " + ChatColor.DARK_AQUA + fromWorldString);
            return;
        }
        if(toWorld == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " Multiverse doesn't know about: " + ChatColor.DARK_AQUA + toWorldString);
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
