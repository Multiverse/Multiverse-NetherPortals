package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.display.ContentDisplay;
import org.mvplugins.multiverse.core.display.handlers.PagedSendHandler;
import org.mvplugins.multiverse.core.display.parsers.ListContentProvider;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Description("Displays a nicely formatted list of all portal links.")
    void onListCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("Portal type to list.")
            @Nullable String linkTypeString
    ) {
        WorldLinkType linkType = null;
        if (linkTypeString != null && !linkTypeString.isEmpty()) {
            linkType = WorldLinkType.valueOf(linkTypeString.toUpperCase(Locale.ROOT));
        }

        String linkString = parseTypeString(linkType);
        ContentDisplay.create()
                .addContent(ListContentProvider.forContent(buildLinkContent(linkType)))
                .withSendHandler(PagedSendHandler.create()
                        .withHeader(String.format("%s==== [ %s %sPortal Links ] ====", ChatColor.DARK_PURPLE, linkString, ChatColor.DARK_PURPLE))
                        .noContentMessage(String.format("%sNo %s %slinks found.", ChatColor.WHITE, linkString, ChatColor.WHITE))
                        .doPagination(false))
                .send(issuer);
    }

    private String parseTypeString(@Nullable WorldLinkType linkType) {
        if (linkType == null) {
            return "All";
        }
        return switch (linkType) {
            case NETHER -> ChatColor.RED + "Nether";
            case END -> ChatColor.AQUA + "End";
        };
    }

    private List<String> buildLinkContent(@Nullable WorldLinkType linkType) {
        return linkType == null ? getAllLinksContent() : buildLinkContent(linkType, "");
    }

    private List<String> getAllLinksContent() {
        List<String> contents = buildLinkContent(
                WorldLinkType.NETHER,
                ChatColor.DARK_RED + "[" + ChatColor.RED + "Nether" + ChatColor.DARK_RED + "] "
        );
        contents.addAll(buildLinkContent(
                WorldLinkType.END,
                ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "End" + ChatColor.DARK_AQUA + "] "
        ));
        return contents;
    }

    private List<String> buildLinkContent(@NotNull WorldLinkType linkType,
                                          @NotNull String prefix) {

        Map<String, String> links = this.linksManager.getLinksForType(linkType);

        return links.entrySet().stream()
                .map(link -> parseSingleLink(link.getKey(), link.getValue(), prefix))
                .collect(Collectors.toList());
    }

    private String parseSingleLink(@NotNull String fromWorldString,
                                   @NotNull String toWorldString,
                                   @NotNull String prefix) {

        return prefix + ChatColor.WHITE + ParseWorldString(fromWorldString) + ChatColor.WHITE + " -> " + ParseWorldString(toWorldString);
    }

    private String ParseWorldString(@NotNull String worldName) {
        return this.worldManager.getLoadedWorld(worldName)
                .map(MultiverseWorld::getAliasOrName)
                .getOrElse(ChatColor.GRAY + worldName + ChatColor.RED + " !!ERROR!!");
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
