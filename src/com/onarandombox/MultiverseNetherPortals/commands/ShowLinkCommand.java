package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.pneumaticraft.commandhandler.Command;

public class ShowLinkCommand extends Command {

    private static final int CMDS_PER_PAGE = 10;

    public ShowLinkCommand(JavaPlugin plugin) {
        super(plugin);
        this.commandName = "Displays all World Links";
        this.commandDesc = "Displays a nicly formatted list of links.";
        this.commandUsage = "/mvnp show " + ChatColor.GOLD + "[PAGE #]";
        this.minimumArgLength = 0;
        this.maximumArgLength = 1;
        this.commandKeys.add("mvnp show");
        this.commandKeys.add("mvnps");
        this.commandKeys.add("mvnpshow");
        this.permission = "multiverse.netherportals.show";
        this.opRequired = true;
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Map<String,String> links = ((MultiverseNetherPortals) this.plugin).getWorldLinks();
        
        if (!(sender instanceof Player)) {
            for (Map.Entry<String,String> link : links.entrySet()) {
                showWorldLink(sender, link.getKey(), link.getValue());
            }
            return;
        }
        int page = 1;
        if(args.size() == 1) {
            try {
                page = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
            }
            if (page < 1) {
                page = 1;
            }
        }
        int totalPages = (int) Math.ceil(links.size() / (CMDS_PER_PAGE + 0.0));

        if (page > totalPages) {
            page = totalPages;
        }
        this.showPage(totalPages, sender, links);

    }

    private void showWorldLink(CommandSender sender, String fromWorldString, String toWorldString) {
        MVWorld fromWorld = null;
        MVWorld toWorld = null;
        fromWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(fromWorldString);
        toWorld = ((MultiverseNetherPortals) this.plugin).getCore().getMVWorld(toWorldString);
        if(fromWorld == null) {
            fromWorldString = ChatColor.RED + "!!ERROR!!";
        } else {
            fromWorldString = fromWorld.getColoredWorldString();
        }
        if(toWorld == null) {
            toWorldString = ChatColor.RED + "!!ERROR!!";
        } else {
            toWorldString = toWorld.getColoredWorldString();
        }
        sender.sendMessage(fromWorldString + ChatColor.WHITE + " -> " + toWorldString);
    }
    
    private void showPage(int page, CommandSender sender, Map<String, String> links) {
        int start = (page - 1) * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        Iterator<Entry<String, String>> entries = links.entrySet().iterator();
        for (Map.Entry<String,String> link : links.entrySet()) {
            showWorldLink(sender, link.getKey(), link.getValue());
        }
        for (int i = 0; i < end; i++) {
            // For consistancy, print some extra lines if it's a player:
            if (entries.hasNext() && i >= start) {
                Map.Entry<String,String>entry = entries.next();
                this.showWorldLink(sender, entry.getKey(), entry.getValue());
            } else if(entries.hasNext()) {
                entries.next();
            } else {
                sender.sendMessage(" ");
            }
        }
    }

}
