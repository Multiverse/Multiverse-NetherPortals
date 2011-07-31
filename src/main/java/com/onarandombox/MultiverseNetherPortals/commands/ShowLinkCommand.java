package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;

public class ShowLinkCommand extends NetherPortalCommand {

    private static final int CMDS_PER_PAGE = 9;

    public ShowLinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Displays all World Links");
        this.setCommandUsage("/mvnp show " + ChatColor.GOLD + "[PAGE #]");
        this.setArgRange(0, 1);
        this.addKey("mvnp show");
        this.addKey("mvnp list");
        this.addKey("mvnpli");
        this.addKey("mvnps");
        this.addKey("mvnplist");
        this.addKey("mvnpshow");
        this.setPermission("multiverse.netherportals.show", "Displays a nicly formatted list of links.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Map<String, String> links = this.plugin.getWorldLinks();

        if (!(sender instanceof Player)) {
            for (Map.Entry<String, String> link : links.entrySet()) {
                showWorldLink(sender, link.getKey(), link.getValue());
            }
            return;
        }
        int page = 1;
        if (args.size() == 1) {
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
        this.showPage(totalPages, sender, links, totalPages);

    }

    private void showWorldLink(CommandSender sender, String fromWorldString, String toWorldString) {
        MVWorld fromWorld = null;
        MVWorld toWorld = null;
        fromWorld = this.plugin.getCore().getMVWorld(fromWorldString);
        toWorld = this.plugin.getCore().getMVWorld(toWorldString);

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

    private void showPage(int page, CommandSender sender, Map<String, String> links, int totalpages) {
        int start = (page - 1) * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (totalpages == 0) {
            sender.sendMessage("You haven't setup " + ChatColor.AQUA + "ANY" + ChatColor.WHITE + " world " + ChatColor.DARK_AQUA + "Links" + ChatColor.WHITE + ".");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "--- NetherPortal Links " + ChatColor.GOLD + "[Page " + page + " of " + totalpages + " ]" + ChatColor.AQUA + "---");
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
