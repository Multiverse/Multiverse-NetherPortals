package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.commandhandler.Command;
import org.bukkit.PortalType;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class NetherPortalCommand extends Command {

    protected MultiverseNetherPortals plugin;

    public NetherPortalCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.plugin = plugin;

    }

    @Override
    public abstract void runCommand(CommandSender arg0, List<String> arg1);

    public PortalType parseType(String value) {
        if (value.equalsIgnoreCase("END")) {
            return PortalType.ENDER;
        } else if (value.equalsIgnoreCase("NETHER")) {
            return PortalType.NETHER;
        }
        return null;
    }
}
