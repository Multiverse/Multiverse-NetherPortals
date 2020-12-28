package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.acf.CommandHelp;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.HelpCommand;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvnp")
public class UsageCommand extends NetherPortalCommand {

    public UsageCommand(MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @HelpCommand
    @Subcommand("help")
    @CommandPermission("multiverse.core.help")
    @Syntax("[filter] [page]")
    @Description("Show Multiverse Command usage.")
    public void onUsageCommand(@NotNull CommandSender sender,
                               @NotNull CommandHelp help) {

        this.plugin.getCore().getMVCommandManager().showUsage(help);
    }
}
