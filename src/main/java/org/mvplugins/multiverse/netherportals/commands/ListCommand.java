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
import org.mvplugins.multiverse.netherportals.utils.MVLinkChecker;

import java.util.LinkedHashMap;
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
    private final MVLinkChecker linkChecker;

    @Inject
    ListCommand(@NotNull LinksManager linksManager, @NotNull WorldManager worldManager, @NotNull MVLinkChecker linkChecker) {
        this.linksManager = linksManager;
        this.worldManager = worldManager;
        this.linkChecker = linkChecker;
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

    private List<String> buildLinkContent(@NotNull MVCommandIssuer issuer, @Nullable WorldLinkType linkType) {
        return linkType == null
                ? getAllLinksContent(issuer)
                : buildLinkContentForType(issuer, linkType);
    }

    private List<String> getAllLinksContent(@NotNull MVCommandIssuer issuer) {
        List<String> contents = buildLinkContentForType(issuer, WorldLinkType.NETHER);
        contents.addAll(buildLinkContentForType(issuer, WorldLinkType.END));
        return contents;
    }

    private List<String> buildLinkContentForType(@NotNull MVCommandIssuer issuer, @NotNull WorldLinkType linkType) {
        Map<String, String> links = this.linksManager.getLinksMapForType(linkType);
        Map<String, LinkRow> linkRows = new LinkedHashMap<>();
        links.forEach((fromWorld, toWorld) -> {
            if (linkRows.containsKey(toWorld) && fromWorld.equals(linkRows.get(toWorld).toWorld)) {
                linkRows.get(toWorld).twoWay = true;
            } else {
                linkRows.put(fromWorld, new LinkRow(linkType, fromWorld, toWorld));
            }
        });

        worldManager.getWorlds().forEach(world -> {
            if (links.containsKey(world.getName())) {
                return;
            }
            String toWorldName = linkChecker.getAutoLink(world.getName(), linkType.toPortalType());
            if (toWorldName == null || !worldManager.isWorld(toWorldName)) {
                return;
            }
            if (linkRows.containsKey(toWorldName) && linkRows.get(toWorldName).auto) {
                linkRows.get(toWorldName).twoWay = true;
                return;
            }

            LinkRow linkRow = new LinkRow(linkType, world.getName(), toWorldName);
            linkRow.auto = true;
            linkRows.put(world.getName(), linkRow);
        });

        return linkRows.values().stream()
                .map(LinkRow::getRowMessage)
                .map(message -> message.formatted(issuer))
                .collect(Collectors.toList());
    }

    private static class LinkRow {
        private final WorldLinkType linkType;
        private final String fromWorld;
        private final String toWorld;
        private boolean twoWay = false;
        private boolean auto = false;

        private LinkRow(WorldLinkType linkType, String fromWorld, String toWorld) {
            this.linkType = linkType;
            this.fromWorld = fromWorld;
            this.toWorld = toWorld;
        }

        private Message getRowMessage() {
            if (fromWorld.equals(toWorld)) {
                // link disabled
                return Message.of("{linkType} &f{fromWorld} &7&l-- &cDISABLED",
                        replace("{fromWorld}").with(fromWorld),
                        replace("{linkType}").with(getLinkTypeColoured(linkType)));
            } else if (twoWay) {
                // two-way link
                return Message.of("{linkType} &f{fromWorld} &7&l<--->&r &f{toWorld}{auto}",
                        replace("{fromWorld}").with(fromWorld),
                        replace("{linkType}").with(getLinkTypeColoured(linkType)),
                        replace("{toWorld}").with(toWorld),
                        replace("{auto}").with(autoString()));
            }
            // one-way link
            return Message.of("{linkType} &f{fromWorld} &7&l-->&r &f{toWorld}{auto}",
                    replace("{fromWorld}").with(fromWorld),
                    replace("{linkType}").with(getLinkTypeColoured(linkType)),
                    replace("{toWorld}").with(toWorld),
                    replace("{auto}").with(autoString()));
        }

        private String autoString() {
            return auto ? " &6&o(auto)" : "";
        }

        private Message getLinkTypeColoured(@NotNull WorldLinkType linkType) {
            return switch (linkType) {
                case NETHER -> Message.of("&4[&cnether&4]");
                case END -> Message.of("&3[&bend&3]");
            };
        }
    }

    @Service
    private final static class LegacyAlias extends ListCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(LinksManager linksManager, WorldManager worldManager, MVLinkChecker linkChecker) {
            super(linksManager, worldManager, linkChecker);
        }

        @Override
        @CommandAlias("mvnplist|mvnpli")
        void onListCommand(MVCommandIssuer issuer, String linkTypeString) {
            super.onListCommand(issuer, linkTypeString);
        }
    }
}
