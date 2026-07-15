package org.mvplugins.multiverse.netherportals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
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
    @CommandCompletion("@worldlinktypes @worldswithlink")
    @Syntax("<nether|end> [fromWorld]")
    @Description("{@@mv-netherportals.unlink.description}")
    public void onUnlinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Syntax("<nether|end>")
            @Description("{@@mv-netherportals.unlink.type.description}")
            @NotNull WorldLinkType worldLinkType,

            @Syntax("<fromWorld>")
            @Description("{@@mv-netherportals.unlink.fromworld.description}")
            @NotNull String fromWorldString
    ) {
        String fromWorldName = resolveFromWorldString(fromWorldString)
                .map(MultiverseWorld::getName)
                .getOrElse(fromWorldString); // fallback as its possible world was already deleted!
        String toWorldName = this.linksManager.getWorldLink(fromWorldName, worldLinkType).getOrNull();

        if (!this.linksManager.removeWorldLink(fromWorldName, worldLinkType)) {
            issuer.sendMessage(MVNPi18n.UNLINK_NOTLINKED,
                    Replace.WORLD.with(fromWorldString),
                    replace("{linkType}").with(worldLinkType));
            return;
        }

        this.linksManager.save()
                .onFailure(error -> issuer.sendError(MVNPi18n.UNLINK_FAILED,
                        Replace.ERROR.with(error)))
                .onSuccess(ignore -> {
                    if (fromWorldName.equals(toWorldName)) {
                        issuer.sendMessage(MVNPi18n.UNLINK_ENABLED,
                                replace("{linkType}").with(worldLinkType),
                                Replace.WORLD.with(fromWorldString));
                        return;
                    }
                    issuer.sendMessage(MVNPi18n.UNLINK_SUCCESS,
                            replace("{linkType}").with(worldLinkType),
                            replace("{fromWorld}").with(fromWorldString),
                            replace("{toWorld}").with(toWorldName));
                });
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
        public void onUnlinkCommand(MVCommandIssuer issuer, WorldLinkType worldLinkType, String fromWorldString) {
            super.onUnlinkCommand(issuer, worldLinkType, fromWorldString);
        }
    }
}
