package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.commandTools.contexts.PageFilter;
import com.onarandombox.MultiverseCore.commandTools.display.ContentCreator;
import com.onarandombox.MultiverseCore.commandTools.display.page.PageDisplay;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.PortalType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("mvnp")
public class ListCommand extends NetherPortalCommand {

    public ListCommand(MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @Subcommand("listall")
    @CommandPermission("multiverse.netherportals.list.all")
    @Syntax("[filter] [page]")
    @Description("Displays a nicely formatted list of all portal links.")
    public void onListAllCommand(@NotNull CommandSender sender,
                                 @NotNull PageFilter pageFilter) {

        new PageDisplay().withSender(sender)
                .withHeader(String.format("%s==== [ All Portal Links ] ====", ChatColor.DARK_PURPLE))
                .withCreator(getAllLinksContent())
                .withPageFilter(pageFilter)
                .withEmptyMessage("No portal links found.")
                .build()
                .runTaskAsynchronously(this.plugin);
    }

    private ContentCreator<List<String>> getAllLinksContent() {
        return () -> {
            List<String> contents = buildLinkContent(
                    PortalType.NETHER,
                    ChatColor.DARK_RED + "[" + ChatColor.RED + "Nether" + ChatColor.DARK_RED + "] "
            );
            contents.addAll(buildLinkContent(
                    PortalType.ENDER,
                    ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "End" + ChatColor.DARK_AQUA + "] "
            ));
            return contents;
        };
    }

    @Subcommand("list")
    @CommandPermission("multiverse.netherportals.list")
    @Syntax("<nether|end> [filter] [page]")
    @CommandCompletion("@linkTypes")
    @Description("Displays a nicely formatted list of nether or end links.")
    public void onListCommand(@NotNull CommandSender sender,

                              @Syntax("<nether|end>")
                              @Description("Portal link type to display.")
                              @NotNull PortalType linkType,

                              @NotNull PageFilter pageFilter) {

        String linkString = parseTypeString(linkType);

        new PageDisplay().withSender(sender)
                .withHeader(String.format("%s==== [ %s %sPortal Links ] ====", ChatColor.DARK_PURPLE, linkString, ChatColor.DARK_PURPLE))
                .withCreator(getPortalLinksContent(linkType))
                .withPageFilter(pageFilter)
                .withEmptyMessage(String.format("No %s %slinks found.", linkString, ChatColor.WHITE))
                .build()
                .runTaskAsynchronously(this.plugin);
    }

    private ContentCreator<List<String>> getPortalLinksContent(PortalType linkType) {
        return () -> buildLinkContent(linkType);
    }

    private String parseTypeString(@NotNull PortalType linkType) {
        return (linkType == PortalType.NETHER)
                ? ChatColor.RED + "Nether"
                : ChatColor.AQUA + "End";
    }

    private List<String> buildLinkContent(@NotNull PortalType linkType) {
        return buildLinkContent(linkType, "");
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

        return prefix + ParseWorldString(fromWorldString) + ChatColor.WHITE + " -> " + ParseWorldString(toWorldString);
    }

    private String ParseWorldString(@NotNull String worldName) {
        MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(worldName);
        return (world == null)
                ? ChatColor.GRAY + worldName + ChatColor.RED + " !!ERROR!!"
                : world.getColoredWorldString();
    }
}
