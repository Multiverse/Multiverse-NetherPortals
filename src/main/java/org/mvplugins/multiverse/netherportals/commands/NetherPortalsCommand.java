package org.mvplugins.multiverse.netherportals.commands;

import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.MultiverseCommand;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Contract;

/**
 * Base class for all NetherPortals commands.
 */
@Contract
public abstract class NetherPortalsCommand extends MultiverseCommand {
    protected NetherPortalsCommand(@NotNull MVCommandManager commandManager) {
        super(commandManager, "mvnp");
    }
}
