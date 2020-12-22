package com.onarandombox.MultiverseNetherPortals.commands_acf;

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

    //TODO: List all command.

    @Subcommand("list")
    @CommandPermission("multiverse.netherportals.list")
    @Syntax("<nether|end> [filter] [page]")
    @CommandCompletion("@linkTypes")
    @Description("Displays a nicely formatted list of nether or end links.")
    public void onShowLinkCommand(@NotNull CommandSender sender,
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
        Map<String, String> links = (linkType == PortalType.NETHER)
                ? this.plugin.getWorldLinks()
                : this.plugin.getEndWorldLinks();

        return links.entrySet().stream()
                .map(link -> parseSingleLink(link.getKey(), link.getValue()))
                .collect(Collectors.toList());
    }

    private String parseSingleLink(@NotNull String fromWorldString,
                                   @NotNull String toWorldString) {

        return ParseWorldString(fromWorldString) + ChatColor.WHITE + " -> " + ParseWorldString(toWorldString);
    }

    private String ParseWorldString(@NotNull String worldName) {
        MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(worldName);
        return (world == null)
                ? ChatColor.GRAY + worldName + ChatColor.RED + " !!ERROR!!"
                : world.getColoredWorldString();
    }
}
