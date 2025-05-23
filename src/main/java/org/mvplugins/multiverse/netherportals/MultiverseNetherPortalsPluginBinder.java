package org.mvplugins.multiverse.netherportals;

import org.mvplugins.multiverse.core.inject.binder.JavaPluginBinder;
import org.mvplugins.multiverse.core.module.MultiverseModuleBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

public class MultiverseNetherPortalsPluginBinder extends MultiverseModuleBinder<MultiverseNetherPortals> {

    protected MultiverseNetherPortalsPluginBinder(@NotNull MultiverseNetherPortals plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiverseNetherPortals> bindPluginClass(
            ScopedBindingBuilder<MultiverseNetherPortals> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MultiverseNetherPortals.class);
    }
}
