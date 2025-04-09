package org.mvplugins.multiverse.netherportals.commands;

import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.command.MultiverseCommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Contract;

/**
 * Base class for all NetherPortals commands.
 */
@Contract
@CommandAlias("mvnp")
public abstract class NetherPortalsCommand extends MultiverseCommand {
}
