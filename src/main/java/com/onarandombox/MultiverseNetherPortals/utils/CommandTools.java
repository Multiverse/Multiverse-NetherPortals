package com.onarandombox.MultiverseNetherPortals.utils;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.commands_acf.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands_acf.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands_acf.UnlinkCommand;
import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.InvalidCommandArgument;
import org.bukkit.PortalType;
import org.jetbrains.annotations.NotNull;

public class CommandTools {

    private final MultiverseNetherPortals plugin;
    private final MVCommandManager manager;

    public CommandTools(@NotNull MultiverseNetherPortals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCore().getMVCommandManager();

        // Completions

        // Contexts
        this.manager.getCommandContexts().registerContext(PortalType.class, this::derivePortalType);

        // Conditions

        // Commands
        this.manager.registerCommand(new LinkCommand(this.plugin));
        this.manager.registerCommand(new UnlinkCommand(this.plugin));
    }

    private PortalType derivePortalType(BukkitCommandExecutionContext context) {
        String typeString = context.popFirstArg();
        if (typeString.equalsIgnoreCase("end")) {
            return PortalType.ENDER;
        }
        else if (typeString.equalsIgnoreCase("nether")) {
            return PortalType.NETHER;
        }
        throw new InvalidCommandArgument("The portal type must either be 'end' or 'nether'");
    }
}
