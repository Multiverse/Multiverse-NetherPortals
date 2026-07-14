package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.display.ContentDisplay;
import org.mvplugins.multiverse.core.display.handlers.PagedSendHandler;
import org.mvplugins.multiverse.core.display.parsers.ListContentProvider;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.acf.commands.annotation.Values;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace.WORLD;
import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class ListCommand extends NetherPortalsCommand {

    private final LinksManager linksManager;
    private final WorldManager worldManager;

    @Inject
    ListCommand(@NotNull LinksManager linksManager, @NotNull WorldManager worldManager) {
        this.linksManager = linksManager;
        this.worldManager = worldManager;
    }

    // todo page and filter
    @Subcommand("list")
    @CommandPermission("multiverse.netherportals.show") // todo: maybe change to multiverse.netherportals.list
    @CommandCompletion("nether|end")
    @Syntax("[nether|end]")
    @Description("{@@mv-netherportals.list.description}")
    void onListCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("{@@mv-netherportals.list.type.description}")
            @Nullable String linkTypeString
    ) {
        WorldLinkType linkType = null;
        if (linkTypeString != null && !linkTypeString.isEmpty()) {
            linkType = WorldLinkType.valueOf(linkTypeString.toUpperCase(Locale.ROOT));
        }

        Message headerMessage = linkType == null
                ? Message.of(MVNPi18n.LIST_HEADER_ALL)
                : Message.of(MVNPi18n.LIST_HEADER,
                        replace("{linkType}").with(linkType));
        Message noContentMessage = linkType == null
                ? Message.of(MVNPi18n.LIST_NOCONTENT_ALL)
                : Message.of(MVNPi18n.LIST_NOCONTENT,
                        replace("{linkType}").with(linkType));
        ContentDisplay.create()
                .addContent(ListContentProvider.forContent(buildLinkContent(issuer, linkType)))
                .withSendHandler(PagedSendHandler.create()
                        .withHeader(headerMessage)
                        .noContentMessage(noContentMessage)
                        .doPagination(false))
                .send(issuer);
    }

    private List<String> buildLinkContent(
            @NotNull MVCommandIssuer issuer,
            @Nullable WorldLinkType linkType) {
        return linkType == null
                ? getAllLinksContent(issuer)
                : buildLinkContent(issuer, linkType, Message.of(""));
    }

    private List<String> getAllLinksContent(@NotNull MVCommandIssuer issuer) {
        List<String> contents = buildLinkContent(
                issuer,
                WorldLinkType.NETHER,
                getLinkTypePrefix(WorldLinkType.NETHER)
        );
        contents.addAll(buildLinkContent(
                issuer,
                WorldLinkType.END,
                getLinkTypePrefix(WorldLinkType.END)
        ));
        return contents;
    }

    private Message getLinkTypePrefix(@NotNull WorldLinkType linkType) {
        ChatColor primaryColor = linkType == WorldLinkType.NETHER ? ChatColor.RED : ChatColor.AQUA;
        ChatColor secondaryColor = linkType == WorldLinkType.NETHER ? ChatColor.DARK_RED : ChatColor.DARK_AQUA;
        return Message.of(secondaryColor + "[" + primaryColor + linkType.getConfigKey()
                + secondaryColor + "] ");
    }

    private List<String> buildLinkContent(
            @NotNull MVCommandIssuer issuer,
            @NotNull WorldLinkType linkType,
            @NotNull Message prefix) {

        Map<String, String> links = this.linksManager.getLinksForType(linkType);

        return links.entrySet().stream()
                .map(link -> parseSingleLink(issuer, link.getKey(), link.getValue(), prefix))
                .collect(Collectors.toList());
    }

    private String parseSingleLink(
            @NotNull MVCommandIssuer issuer,
            @NotNull String fromWorldString,
            @NotNull String toWorldString,
            @NotNull Message prefix) {
        return Message.of(MVNPi18n.LIST_ENTRY,
                        replace("{prefix}").with(prefix),
                        replace("{fromWorld}").with(parseWorldString(fromWorldString)),
                        replace("{toWorld}").with(parseWorldString(toWorldString)))
                .formatted(issuer);
    }

    private Message parseWorldString(@NotNull String worldName) {
        return this.worldManager.getLoadedWorld(worldName)
                .map(world -> Message.of(world.getAliasOrName()))
                .getOrElse(() -> Message.of(MVNPi18n.LIST_WORLD_NOTFOUND, WORLD.with(worldName)));
    }

    @Service
    private final static class LegacyAlias extends ListCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(LinksManager linksManager, WorldManager worldManager) {
            super(linksManager, worldManager);
        }

        @Override
        @CommandAlias("mvnplist|mvnpli")
        void onListCommand(MVCommandIssuer issuer, String linkTypeString) {
            super.onListCommand(issuer, linkTypeString);
        }
    }
}
