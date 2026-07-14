package org.mvplugins.multiverse.netherportals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.exceptions.command.MVInvalidCommandArgument;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
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
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n;

import java.util.Locale;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace.WORLD;
import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class LinkCommand extends NetherPortalsCommand {

    private final LinksManager linksManager;

    @Inject
    LinkCommand(@NotNull LinksManager linksManager) {
        this.linksManager = linksManager;
    }

    @Subcommand("link")
    @CommandPermission("multiverse.netherportals.link")
    @CommandCompletion("@worldlinktypes @mvworlds:scope=both @mvworlds:scope=both")
    @Syntax("<nether|end> [fromWorld] <toWorld>")
    @Description("{@@mv-netherportals.link.description}")
    public void onLinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Syntax("<nether|end>")
            @Description("{@@mv-netherportals.link.type.description}")
            @NotNull WorldLinkType worldLinkType,

            @Flags("resolve=issuerAware")
            @Syntax("[fromWorld]")
            @Description("{@@mv-netherportals.link.fromworld.description}")
            @NotNull MultiverseWorld fromWorld,

            @Syntax("<toWorld>")
            @Description("{@@mv-netherportals.link.toworld.description}")
            @NotNull MultiverseWorld toWorld
    ) {
        if (!this.linksManager.addWorldLink(fromWorld, toWorld, worldLinkType)
                || this.linksManager.save().isFailure()) {
            throw MVInvalidCommandArgument.of(Message.of(MVNPi18n.LINK_FAILED));
        }

        if (fromWorld.getName().equals(toWorld.getName())) {
            issuer.sendMessage(MVNPi18n.LINK_DISABLED,
                    replace("{linkType}").with(worldLinkType),
                    WORLD.with(toWorld.getAliasOrName()));
            return;
        }

        issuer.sendMessage(MVNPi18n.LINK_SUCCESS,
                replace("{linkType}").with(worldLinkType),
                replace("{fromWorld}").with(fromWorld.getAliasOrName()),
                replace("{toWorld}").with(toWorld.getAliasOrName()));
    }

    @Service
    private final static class LegacyAlias extends LinkCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(LinksManager linksManager) {
            super(linksManager);
        }

        @Override
        @CommandAlias("mvnplink|mvnpl")
        public void onLinkCommand(MVCommandIssuer issuer, WorldLinkType worldLinkType, MultiverseWorld fromWorld, MultiverseWorld toWorld) {
            super.onLinkCommand(issuer, worldLinkType, fromWorld, toWorld);
        }
    }
}
