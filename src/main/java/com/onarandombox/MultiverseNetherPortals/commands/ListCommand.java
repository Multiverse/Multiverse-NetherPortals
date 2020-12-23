package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.commandTools.PageDisplay;
import com.onarandombox.MultiverseCore.commandTools.PageFilter;
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

        List<String> contents = buildLinkContent(PortalType.NETHER,
                ChatColor.DARK_RED + "[" + ChatColor.RED + "Nether" + ChatColor.DARK_RED + "] ");

        contents.addAll(buildLinkContent(PortalType.ENDER,
                ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "End" + ChatColor.DARK_AQUA + "] "));

        PageDisplay pageDisplay = new PageDisplay(
                sender,
                ChatColor.DARK_PURPLE + "==== [ All Portal Links ] ====",
                contents,
                pageFilter
        );

        pageDisplay.showPageAsync(this.plugin);
    }

    @Subcommand("list")
    @CommandPermission("multiverse.netherportals.list")
    @Syntax("<nether|end> [filter] [page]")
    @CommandCompletion("@linkTypes")
    @Description("Displays a nicely formatted list of nether or end links.")
    public void onListCommand(@NotNull CommandSender sender,
                              @NotNull PortalType linkType,
                              @NotNull PageFilter pageFilter) {

        PageDisplay pageDisplay = new PageDisplay(
                sender,
                ChatColor.DARK_PURPLE + "==== [ " + parseTypeString(linkType) + ChatColor.DARK_PURPLE + " Portal Links ] ====",
                buildLinkContent(linkType),
                pageFilter
        );

        pageDisplay.showPageAsync(this.plugin);
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
