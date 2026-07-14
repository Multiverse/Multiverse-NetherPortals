package org.mvplugins.multiverse.netherportals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.exceptions.command.MVInvalidCommandArgument;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.acf.commands.annotation.Values;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n;

import java.util.Locale;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace.WORLD;
import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class UnlinkCommand extends NetherPortalsCommand {

    private final LinksManager linksManager;
    private final CoreConfig coreConfig;
    private final WorldManager worldManager;

    @Inject
    UnlinkCommand(@NotNull LinksManager linksManager,
                  @NotNull CoreConfig coreConfig,
                  @NotNull WorldManager worldManager) {
        this.linksManager = linksManager;
        this.coreConfig = coreConfig;
        this.worldManager = worldManager;
    }

    @Subcommand("unlink")
    @CommandPermission("multiverse.netherportals.unlink")
    @CommandCompletion("nether|end @mvworlds:scope=both")
    @Syntax("<nether|end> [fromWorld]")
    @Description("{@@mv-netherportals.unlink.description}")
    public void onUnlinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("{@@mv-netherportals.unlink.type.description}")
            @NotNull String linkType,

            @Syntax("<fromWorld>")
            @Description("{@@mv-netherportals.unlink.fromworld.description}")
            @NotNull String fromWorldString
    ) {
        Option<MultiverseWorld> fromWorld = resolveFromWorldString(fromWorldString);
        String fromWorldName = fromWorld
                .map(MultiverseWorld::getName)
                .getOrElse(fromWorldString); // fallback as its possible world was already deleted!
        WorldLinkType worldLinkType = WorldLinkType.valueOf(linkType.toUpperCase(Locale.ROOT));
        String toWorldName = this.linksManager.getWorldLink(fromWorldName, worldLinkType).getOrNull();
        if (toWorldName == null) {
            issuer.sendMessage(MVNPi18n.UNLINK_NOTLINKED,
                    WORLD.with(fromWorldString),
                    replace("{linkType}").with(worldLinkType));
            return;
        }

        boolean linkRemoved = fromWorld
                .map(world -> this.linksManager.removeWorldLink(world, worldLinkType))
                .getOrElse(() -> this.linksManager.removeWorldLink(fromWorldName, worldLinkType));
        if (!linkRemoved
                || this.linksManager.save().isFailure()) {
            throw MVInvalidCommandArgument.of(Message.of(MVNPi18n.UNLINK_FAILED));
        }

        if (fromWorldName.equals(toWorldName)) {
            issuer.sendMessage(MVNPi18n.UNLINK_ENABLED,
                    replace("{linkType}").with(worldLinkType),
                    WORLD.with(fromWorldString));
            return;
        }

        issuer.sendMessage(MVNPi18n.UNLINK_SUCCESS,
                replace("{linkType}").with(worldLinkType),
                replace("{fromWorld}").with(fromWorldString),
                replace("{toWorld}").with(toWorldName));
    }

    private Option<MultiverseWorld> resolveFromWorldString(@NotNull String fromWorldString) {
        return coreConfig.getResolveAliasName()
                ? worldManager.getWorldByNameOrAlias(fromWorldString)
                : worldManager.getWorld(fromWorldString);
    }

    @Service
    private final static class LegacyAlias extends UnlinkCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(@NotNull LinksManager linksManager,
                    @NotNull CoreConfig coreConfig,
                    @NotNull WorldManager worldManager) {
            super(linksManager, coreConfig, worldManager);
        }

        @Override
        @CommandAlias("mvnpunlink|mvnpu")
        public void onUnlinkCommand(MVCommandIssuer issuer, String linkType, String fromWorldString) {
            super.onUnlinkCommand(issuer, linkType, fromWorldString);
        }
    }
}
