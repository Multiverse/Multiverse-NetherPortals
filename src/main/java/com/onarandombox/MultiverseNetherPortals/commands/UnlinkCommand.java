package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class UnlinkCommand extends NetherPortalCommand {
    private MVWorldManager worldManager;

    public UnlinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Remove NP Destination");
        this.setCommandUsage("/mvnp unlink " + ChatColor.GOLD + "[FROM_WORLD]");
        this.setArgRange(0, 1);
        this.addKey("mvnp unlink");
        this.addKey("mvnpu");
        this.addKey("mvnpunlink");
        this.setPermission("multiverse.netherportals.unlink", "This will remove a world link that's been set. You do not need to do this before setting a new one.", PermissionDefault.OP);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
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

        fromWorld = this.worldManager.getMVWorld(fromWorldString);
        if (fromWorld == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " Doesn't look like Multiverse knows about '" + fromWorldString + "'");
            return;
        }

        toWorldString = this.plugin.getWorldLink(fromWorld.getName());
        if (toWorldString == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " The world " + fromWorld.getColoredWorldString() + ChatColor.WHITE + " was never linked.");
            return;
        }
        toWorld = this.worldManager.getMVWorld(toWorldString);

        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();
        sender.sendMessage("The Nether Portals in " + coloredFrom + ChatColor.WHITE + " are now " + ChatColor.RED + "unlinked" + ChatColor.WHITE + " from " + coloredTo + ChatColor.WHITE + ".");
        this.plugin.removeWorldLink(fromWorld.getName(), toWorld.getName());
    }

}
