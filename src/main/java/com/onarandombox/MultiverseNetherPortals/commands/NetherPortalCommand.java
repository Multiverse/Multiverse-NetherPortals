package com.onarandombox.MultiverseNetherPortals.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.pneumaticraft.commandhandler.Command;

public abstract class NetherPortalCommand extends Command {

    protected MultiverseNetherPortals plugin;
    public NetherPortalCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.plugin = plugin;

    }

    @Override
    public abstract void runCommand(CommandSender arg0, List<String> arg1);

}
