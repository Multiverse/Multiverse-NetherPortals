package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.acf.InvalidCommandArgument;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.PortalType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvnp")
public class UnlinkCommand extends NetherPortalCommand {

    public UnlinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @Subcommand("unlink")
    @CommandPermission("multiverse.netherportals.link")
    @Syntax("<nether|end> [fromWorld]")
    @CommandCompletion("@linkTypes @MVWorlds|@unloadedWorlds")
    @Description("Sets which world to link to when a player enters a NetherPortal in this world.")
    public void onLinkCommand(@NotNull CommandSender sender,
                              @NotNull PortalType linkType,
                              @Nullable @Optional MultiverseWorld fromWorld,
                              @Nullable @Optional @Flags("trim") String fromWorldString) {

        if (fromWorld == null && fromWorldString == null) {
            throw new InvalidCommandArgument("You need to specify a fromWorld.");
        }

        fromWorldString = (fromWorldString == null)
                ? fromWorld.getName()
                : fromWorldString;

        fromWorld = (fromWorld == null)
                ? this.plugin.getCore().getMVWorldManager().getMVWorld(fromWorldString)
                : fromWorld;

        String coloredFrom = (fromWorld == null) ? fromWorldString : fromWorld.getColoredWorldString();

        String toWorldString = this.plugin.getWorldLink(fromWorldString, linkType);
        if (toWorldString == null) {
            sender.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " The world "
                    + coloredFrom + ChatColor.WHITE + " was never linked.");
            return;
        }

        if (!this.plugin.removeWorldLink(fromWorldString, toWorldString, linkType)) {
            sender.sendMessage(ChatColor.RED + "There was an error unlinking the portals! Please check console for errors.");
            return;
        }

        MultiverseWorld toWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(toWorldString);
        String coloredTo = (toWorld == null) ? toWorldString : toWorld.getColoredWorldString();

        if (fromWorldString.equals(toWorldString)) {
            sender.sendMessage("You have " + ChatColor.GREEN + "successfully enabled " + ChatColor.WHITE
                    + linkType + " portals for world " + coloredFrom + ".");
            return;
        }

        sender.sendMessage("The " + linkType + " portals in " + coloredFrom + ChatColor.WHITE + " are now "
                + ChatColor.RED + "unlinked" + ChatColor.WHITE + " from " + coloredTo + ChatColor.WHITE + ".");
    }
}
