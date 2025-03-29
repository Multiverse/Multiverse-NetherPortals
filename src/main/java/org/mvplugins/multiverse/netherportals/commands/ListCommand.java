package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
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
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CommandAlias("mvnp")
class ListCommand extends NetherPortalsCommand {

    private final MultiverseNetherPortals plugin;
    private final WorldManager worldManager;

    @Inject
    ListCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull MultiverseNetherPortals plugin,
            @NotNull WorldManager worldManager) {
        super(commandManager);
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    // todo page and filter
    @CommandAlias("mvnplist|mvnpli")
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
        PortalType linkType = null;
        if (linkTypeString != null && !linkTypeString.isEmpty()) {
            if (linkTypeString.equalsIgnoreCase("nether")) {
                linkType = PortalType.NETHER;
            } else if (linkTypeString.equalsIgnoreCase("end")) {
                linkType = PortalType.ENDER;
            }
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

    private String parseTypeString(@Nullable PortalType linkType) {
        if (linkType == null) {
            return "All";
        }
        return switch (linkType) {
            case NETHER -> ChatColor.RED + "Nether";
            case ENDER -> ChatColor.AQUA + "End";
            default -> "All";
        };
    }

    private List<String> buildLinkContent(@Nullable PortalType linkType) {
        return linkType == null ? getAllLinksContent() : buildLinkContent(linkType, "");
    }

    private List<String> getAllLinksContent() {
        List<String> contents = buildLinkContent(
                PortalType.NETHER,
                ChatColor.DARK_RED + "[" + ChatColor.RED + "Nether" + ChatColor.DARK_RED + "] "
        );
        contents.addAll(buildLinkContent(
                PortalType.ENDER,
                ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "End" + ChatColor.DARK_AQUA + "] "
        ));
        return contents;
    }

    private List<String> buildLinkContent(@NotNull PortalType linkType,
                                          @NotNull String prefix) {

        Map<String, String> links = (linkType == PortalType.NETHER)
                ? this.plugin.getWorldLinks()
                : this.plugin.getEndWorldLinks();

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
                .map(MultiverseWorld::getAlias)
                .getOrElse(ChatColor.GRAY + worldName + ChatColor.RED + " !!ERROR!!");
    }
}
