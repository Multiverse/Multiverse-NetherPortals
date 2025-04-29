package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.acf.commands.annotation.Values;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.Objects;

@Service
class UnlinkCommand extends NetherPortalsCommand {

    private final MultiverseNetherPortals plugin;

    @Inject
    UnlinkCommand(@NotNull MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    @Subcommand("unlink")
    @CommandPermission("multiverse.netherportals.unlink")
    @CommandCompletion("nether|end @mvworlds:scope=both")
    @Syntax("<nether|end> [fromWorld]")
    @Description("This will remove a world link that's been set. You do not need to do this before setting a new one.")
    public void onUnlinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("Portal type to unlink.")
            @NotNull String linkType,

            @Syntax("<fromWorld>")
            @Description("World the portals are at.")
            @NotNull String fromWorldString
    ) {
        PortalType portalType = Objects.equals(linkType, "nether") ? PortalType.NETHER : PortalType.ENDER;
        String toWorldString = this.plugin.getWorldLink(fromWorldString, portalType);
        if (toWorldString == null) {
            issuer.sendMessage(ChatColor.RED + "Whoops!" + ChatColor.WHITE + " The world "
                    + fromWorldString + ChatColor.WHITE + " was never linked.");
            return;
        }

        if (!this.plugin.removeWorldLink(fromWorldString, toWorldString, portalType)) {
            throw new InvalidCommandArgument("There was an issue unlinking the portals! Please check console for errors.");
        }

        if (fromWorldString.equals(toWorldString)) {
            issuer.sendMessage(String.format("You have %ssuccessfully enabled %s%s portals for world %s.",
                    ChatColor.GREEN, ChatColor.WHITE, linkType, fromWorldString));
            return;
        }

        issuer.sendMessage(String.format("The %s portals in %s%s are now %sunlinked %sfrom %s%s.",
                linkType, fromWorldString, ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE, toWorldString, ChatColor.WHITE));
    }

    @Service
    private final static class LegacyAlias extends UnlinkCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiverseNetherPortals plugin) {
            super(plugin);
        }

        @Override
        @CommandAlias("mvnpunlink|mvnpu")
        public void onUnlinkCommand(MVCommandIssuer issuer, String linkType, String fromWorldString) {
            super.onUnlinkCommand(issuer, linkType, fromWorldString);
        }
    }
}
