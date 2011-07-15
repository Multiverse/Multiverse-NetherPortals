package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.pneumaticraft.commandhandler.Command;

public class UnlinkCommand extends Command {

    public UnlinkCommand(JavaPlugin plugin) {
        super(plugin);
        this.commandName = "Remove NP Destination";
        this.commandDesc = "This will remove a world link that's been set. You do not need to do this before setting a new one.";
        this.commandUsage = "/mvnp unlink " + ChatColor.GOLD + "[FROM_WORLD]";
        this.minimumArgLength = 0;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvnpunlink");
        this.commandKeys.add("mvnpu");
        this.commandKeys.add("mvnp unlink");
        this.permission = "multiverse.netherportals.unlink";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player) && args.size() == 0) {
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
        } else {
            fromWorldString = args.get(0);
        }

        fromWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(fromWorldString);
        if (fromWorld == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " Doesn't look like Multiverse knows about '" + fromWorldString + "'");
            return;
        }

        toWorldString = ((MultiverseNetherPortals) this.plugin).getWorldLink(fromWorld.getName());
        if (toWorldString == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " The world " + fromWorld.getColoredWorldString() + ChatColor.WHITE + " was never linked.");
            return;
        }
        toWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(toWorldString);

        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();
        sender.sendMessage("The Nether Portals in " + coloredFrom + ChatColor.WHITE + " are now " + ChatColor.RED + "unlinked" + ChatColor.WHITE + " from " + coloredTo + ChatColor.WHITE + ".");
    }

}
