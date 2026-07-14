package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.acf.commands.annotation.Values;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;

import java.util.Locale;

@Service
class LinkCommand extends NetherPortalsCommand {

    private final LinksManager linksManager;

    @Inject
    LinkCommand(@NotNull LinksManager linksManager) {
        this.linksManager = linksManager;
    }

    @Subcommand("link")
    @CommandPermission("multiverse.netherportals.link")
    @CommandCompletion("nether|end @mvworlds @mvworlds")
    @Syntax("<nether|end> [fromWorld] <toWorld>")
    @Description("Sets which world to link to when a player enters a NetherPortal in this world.")
    public void onLinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("Portal type to link.")
            @NotNull String linkType,

            @Flags("resolve=issuerAware")
            @Syntax("[fromWorld]")
            @Description("World the portals are at.")
            @NotNull MultiverseWorld fromWorld,

            @Syntax("<toWorld>")
            @Description("World the portals should teleport to.")
            @NotNull MultiverseWorld toWorld
    ) {
        WorldLinkType worldLinkType = WorldLinkType.valueOf(linkType.toUpperCase(Locale.ROOT));
        if (!this.linksManager.addWorldLink(fromWorld, toWorld, worldLinkType)
                || this.linksManager.save().isFailure()) {
            throw new InvalidCommandArgument("There was an error creating the link! See console for more details.");
        }

        String coloredFrom = fromWorld.getAliasOrName();
        String coloredTo = toWorld.getAliasOrName();

        issuer.sendMessage((fromWorld.getName().equals(toWorld.getName()))
                ? String.format("%sNOTE: %sYou have %ssuccessfully disabled %s%s Portals in %s.",
                ChatColor.RED, ChatColor.WHITE, ChatColor.GREEN, ChatColor.WHITE, linkType, coloredTo)
                : String.format("The %s portals in %s%s are now linked to %s.",
                linkType, coloredFrom, ChatColor.WHITE, coloredTo));
    }

    @Service
    private final static class LegacyAlias extends LinkCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(LinksManager linksManager) {
            super(linksManager);
        }

        @Override
        @CommandAlias("mvnplink|mvnpl")
        public void onLinkCommand(MVCommandIssuer issuer, String linkType, MultiverseWorld fromWorld, MultiverseWorld toWorld) {
            super.onLinkCommand(issuer, linkType, fromWorld, toWorld);
        }
    }
}
