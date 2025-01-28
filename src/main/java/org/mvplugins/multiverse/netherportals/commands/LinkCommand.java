package org.mvplugins.multiverse.netherportals.commands;

import org.bukkit.ChatColor;
import org.bukkit.PortalType;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.acf.commands.annotation.Values;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.Objects;

@Service
@CommandAlias("mvnp")
class LinkCommand extends NetherPortalsCommand {

    private final MultiverseNetherPortals plugin;

    @Inject
    LinkCommand(@NotNull MVCommandManager commandManager, @NotNull MultiverseNetherPortals plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvnplink|mvnpl")
    @Subcommand("link")
    @CommandPermission("multiverse.netherportals.link")
    @CommandCompletion("nether|end @mvworlds @mvworlds")
    @Syntax("<nether|end> [fromWorld] <toWorld>")
    @Description("Sets which world to link to when a player enters a NetherPortal in this world.")
    public void onLinkCommand(
            @NotNull MVCommandIssuer issuer,

            @Values("nether|end")
            @Syntax("<nether|end>")
            @Description("Portal type to link.")
            @NotNull String linkType,

            @Flags("resolve=issuerAware")
            @Syntax("[fromWorld]")
            @Description("World the portals are at.")
            @NotNull MultiverseWorld fromWorld,

            @Syntax("<toWorld>")
            @Description("World the portals should teleport to.")
            @NotNull MultiverseWorld toWorld
    ) {
        PortalType portalType = Objects.equals(linkType, "nether") ? PortalType.NETHER : PortalType.ENDER;
        if (!this.plugin.addWorldLink(fromWorld.getName(), toWorld.getName(), portalType)) {
            throw new InvalidCommandArgument("There was an error creating the link! See console for more details.");
        }

        String coloredFrom = fromWorld.getAlias();
        String coloredTo = toWorld.getAlias();

        issuer.sendMessage((fromWorld.getName().equals(toWorld.getName()))
                ? String.format("%sNOTE: %sYou have %ssuccessfully disabled %s%s Portals in %s.",
                ChatColor.RED, ChatColor.WHITE, ChatColor.GREEN, ChatColor.WHITE, linkType, coloredTo)
                : String.format("The %s portals in %s%s are now linked to %s.",
                linkType, coloredFrom, ChatColor.WHITE, coloredTo));
    }
}
