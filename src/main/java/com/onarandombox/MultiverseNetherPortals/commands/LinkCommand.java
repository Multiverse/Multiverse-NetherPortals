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
public class LinkCommand extends NetherPortalCommand {

    public LinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @Subcommand("link")
    @CommandPermission("multiverse.netherportals.link")
    @Syntax("<nether|end> [fromWorld] <toWorld>")
    @CommandCompletion("@linkTypes @MVWorlds @MVWorlds")
    @Description("Sets which world to link to when a player enters a NetherPortal in this world.")
    public void onLinkCommand(@NotNull CommandSender sender,
                              @Nullable @Optional MultiverseWorld playerWorld,

                              @Syntax("<nether|end>")
                              @Description("Portal type to link.")
                              @NotNull PortalType linkType,

                              @Syntax("[fromWorld]")
                              @Description("World the portals are at.")
                              @NotNull @Flags("other") MultiverseWorld fromWorld,

                              @Syntax("<toWorld>")
                              @Description("World the portals should teleport to.")
                              @Nullable @Optional @Flags("other") MultiverseWorld toWorld) {

        if (toWorld == null && playerWorld == null) {
            throw new InvalidCommandArgument("You need to specify a toWorld.");
        }

        if (toWorld == null) {
            toWorld = fromWorld;
            fromWorld = playerWorld;
        }

        if (!this.plugin.addWorldLink(fromWorld.getName(), toWorld.getName(), linkType)) {
             throw new InvalidCommandArgument("There was an error creating the link! See console for more details.");
        }

        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();

        sender.sendMessage((fromWorld.getName().equals(toWorld.getName()))

                ? String.format("%sNOTE: %sYou have %ssuccessfully disabled %s%s Portals in %s.",
                ChatColor.RED, ChatColor.WHITE, ChatColor.GREEN, ChatColor.WHITE, linkType, coloredTo)

                : String.format("The %s portals in %s%s are now linked to %s.",
                linkType, coloredFrom, ChatColor.WHITE, coloredTo));
    }
}
