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

                              @Syntax("<nether|end>")
                              @Description("Portal type to unlink.")
                              @NotNull PortalType linkType,

                              @Syntax("[fromWorld]")
                              @Description("World the portals are at.")
                              @Nullable @Optional MultiverseWorld fromWorld,

                              // Possible to be unloaded/deleted, so we dont use MultiverseWorld.
                              @Nullable @Optional @Flags("trim") String fromWorldString) {

        if (fromWorld == null && fromWorldString == null) {
            throw new InvalidCommandArgument("You need to specify a fromWorld in console.");
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
            throw new InvalidCommandArgument("There was an issue unlinking the portals! Please check console for errors.");
        }

        MultiverseWorld toWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(toWorldString);
        String coloredTo = (toWorld == null) ? toWorldString : toWorld.getColoredWorldString();

        if (fromWorldString.equals(toWorldString)) {
            sender.sendMessage(String.format("You have %ssuccessfully enabled %s%s portals for world %s.",
                    ChatColor.GREEN, ChatColor.WHITE, linkType, coloredFrom));
            return;
        }

        sender.sendMessage(String.format("The %s portals in %s%s are now %sunlinked %sfrom %s%s.",
                linkType, coloredFrom, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE, coloredTo, ChatColor.WHITE));
    }
}
