package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.commandTools.ColourAlternator;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.acf.annotation.CommandAlias;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCommand extends NetherPortalCommand {

    public RootCommand(MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @CommandAlias("mvnp")
    public void onRootCommand(@NotNull CommandSender sender) {
        this.plugin.getCore().getMVCommandManager().showPluginInfo(
                sender,
                this.plugin.getDescription(),
                new ColourAlternator(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE),
                "mvnp"
        );
    }
}
