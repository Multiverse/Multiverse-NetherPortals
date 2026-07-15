package org.mvplugins.multiverse.netherportals.command;

import org.bukkit.Difficulty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandCompletions;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandCompletionContext;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.links.WorldLink;
import org.mvplugins.multiverse.netherportals.links.WorldLinkType;

import java.util.Collection;
import java.util.Collections;

@ApiStatus.Internal
@Service
public class MVNPCommandCompletions {

    private final LinksManager linksManager;

    @Inject
    private MVNPCommandCompletions(@NotNull MVCommandManager commandManager, @NotNull LinksManager linksManager) {
        this.linksManager = linksManager;

        MVCommandCompletions commandCompletions = commandManager.getCommandCompletions();
        commandCompletions.registerStaticCompletion("worldlinktypes", commandCompletions.suggestEnums(WorldLinkType.class));
        commandCompletions.registerAsyncCompletion("worldswithlink", this::suggestWorldsWithLinks);
    }

    private Collection<String> suggestWorldsWithLinks(BukkitCommandCompletionContext context) {
        return Try.of(() -> {
            WorldLinkType linkType = context.getContextValue(WorldLinkType.class);
            return linksManager.getWorldLinks().stream()
                    .filter(worldLink -> worldLink.getLinkTo(linkType).isDefined())
                    .map(WorldLink::getFrom)
                    .toList();
        }).getOrElse(Collections.emptyList());
    }
}
