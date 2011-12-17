package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ShowLinkCommand extends NetherPortalCommand {

    private static final int CMDS_PER_PAGE = 10;
    private MVWorldManager worldManager;

    public ShowLinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Displays all World Links");
        this.setCommandUsage("/mvnp show " + ChatColor.GOLD + "[type] [PAGE #]");
        this.setArgRange(0, 2);
        this.addKey("mvnp show");
        this.addKey("mvnp list");
        this.addKey("mvnpli");
        this.addKey("mvnps");
        this.addKey("mvnplist");
        this.addKey("mvnpshow");
        this.setPermission("multiverse.netherportals.show", "Displays a nicely formatted list of links.", PermissionDefault.OP);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Map<String, String> links = this.plugin.getWorldLinks();
        Map<String, String> endlinks = this.plugin.getEndWorldLinks();

        boolean end = false; // true to display end; false otherwise
        if(args.size() > 0) {
            if(args.get(0).equalsIgnoreCase("end")) {
                end = true;
            }
        }

        if (!(sender instanceof Player)) {
            if(!end) {
                sender.sendMessage(ChatColor.AQUA + "--- NetherPortal Links ---");
                for (Map.Entry<String, String> link : links.entrySet()) {
                    showWorldLink(sender, link.getKey(), link.getValue());
                }
            } else {
                sender.sendMessage(ChatColor.AQUA + "--- EnderPortal Links ---");
                for (Map.Entry<String, String> link : endlinks.entrySet()) {
                    showWorldLink(sender, link.getKey(), link.getValue());
                }
            }
            return;
        }
        int page = 1;
        if (args.size() > 0) {
            try {
                page = Integer.parseInt(args.get(args.size() - 1));
            } catch (NumberFormatException e) {
            }
            if (page < 1) {
                page = 1;
            }
        }
        Map<String, String> displayLinks = (end ? endlinks : links);
        int totalPages = (int) Math.ceil(displayLinks.size() / (CMDS_PER_PAGE + 0.0));

        if (page > totalPages) {
            page = totalPages;
        }
        this.showPage(page, sender, displayLinks, totalPages, (end ? "EnderPortals" : "NetherPortals"));
    }

    private void showWorldLink(CommandSender sender, String fromWorldString, String toWorldString) {
        MultiverseWorld fromWorld = this.worldManager.getMVWorld(fromWorldString);
        MultiverseWorld toWorld = this.worldManager.getMVWorld(toWorldString);

        if (fromWorld == null) {
            fromWorldString = ChatColor.RED + "!!ERROR!!";
        } else {
            fromWorldString = fromWorld.getColoredWorldString();
        }
        if (toWorld == null) {
            toWorldString = ChatColor.RED + "!!ERROR!!";
        } else {
            toWorldString = toWorld.getColoredWorldString();
        }
        sender.sendMessage(fromWorldString + ChatColor.WHITE + " -> " + toWorldString);
    }

    private void showPage(int page, CommandSender sender, Map<String, String> links, int totalpages, String headerLabel) {
        int start = (page - 1) * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (totalpages == 0) {
            sender.sendMessage("You haven't setup " + ChatColor.AQUA + "ANY" + ChatColor.WHITE + " world " + ChatColor.DARK_AQUA + "Links" + ChatColor.WHITE + ".");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "--- " + headerLabel + " Links " + ChatColor.LIGHT_PURPLE + "[Page " + page + " of " + totalpages + " ]" + ChatColor.AQUA + "---");
        Iterator<Entry<String, String>> entries = links.entrySet().iterator();
        for (int i = 0; i < end; i++) {
            if (entries.hasNext() && i >= start) {
                Map.Entry<String, String> entry = entries.next();
                this.showWorldLink(sender, entry.getKey(), entry.getValue());
            } else if (entries.hasNext()) {
                entries.next();
            }
        }
    }

}
