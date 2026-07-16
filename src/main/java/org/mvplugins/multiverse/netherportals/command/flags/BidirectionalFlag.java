package org.mvplugins.multiverse.netherportals.command.flags;

import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.flag.CommandFlag;
import org.mvplugins.multiverse.core.command.flag.CommandFlagsManager;
import org.mvplugins.multiverse.core.command.flag.FlagBuilder;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@Service
public class BidirectionalFlag extends FlagBuilder {

    public static final String NAME = "bidirectional";

    @Inject
    private BidirectionalFlag(@NotNull CommandFlagsManager flagsManager) {
        super(NAME, flagsManager);
    }

    public final CommandFlag bidirectional = flag(CommandFlag.builder("--bidirectional")
            .addAlias("-b")
            .build());
}
