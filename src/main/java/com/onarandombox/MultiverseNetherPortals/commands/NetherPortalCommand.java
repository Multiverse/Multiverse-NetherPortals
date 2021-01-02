package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.acf.BaseCommand;

public abstract class NetherPortalCommand extends BaseCommand {

    protected final MultiverseNetherPortals plugin;

    protected NetherPortalCommand(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }
}
