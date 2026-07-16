package org.mvplugins.multiverse.netherportals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;
import org.mvplugins.multiverse.netherportals.links.LinksManager;
import org.mvplugins.multiverse.netherportals.utils.customportals.CustomPortalsHandler;

/**
 * Provides access to the Multiverse-NetherPortals API.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
public final class MultiverseNetherPortalsApi {

    private static MultiverseNetherPortalsApi instance;
    private static final List<Consumer<MultiverseNetherPortalsApi>> WHEN_LOADED_CALLBACKS = new ArrayList<>();

    /**
     * Initializes the API.
     *
     * @param multiverseNetherPortals The Multiverse-NetherPortals plugin
     */
    static void init(@NotNull MultiverseNetherPortals multiverseNetherPortals) {
        if (instance != null) {
            throw new IllegalStateException("MultiverseNetherPortalsApi has already been initialized!");
        }
        instance = new MultiverseNetherPortalsApi(multiverseNetherPortals.getServiceLocator());
        Bukkit.getServicesManager().register(
                MultiverseNetherPortalsApi.class,
                instance,
                multiverseNetherPortals,
                ServicePriority.Normal);

        List<Consumer<MultiverseNetherPortalsApi>> callbacks = List.copyOf(WHEN_LOADED_CALLBACKS);
        WHEN_LOADED_CALLBACKS.clear();
        MultiverseNetherPortalsApi loadedApi = instance;
        callbacks.forEach(callback -> runLoadCallback(callback, loadedApi));
    }

    private static void runLoadCallback(
            @NotNull Consumer<MultiverseNetherPortalsApi> callback,
            @NotNull MultiverseNetherPortalsApi loadedApi) {
        Try.run(() -> callback.accept(loadedApi))
                .onFailure(exception -> Logging.warning(
                        "A Multiverse-NetherPortals API load callback failed: %s", exception.getMessage()));
    }

    /**
     * Shuts down the API.
     */
    static void shutdown() {
        if (instance == null) {
            return;
        }
        Bukkit.getServicesManager().unregister(instance);
        instance = null;
    }

    /**
     * Executes a callback once the Multiverse-NetherPortals API has been initialized.
     * The callback is executed immediately if the API is already initialized.
     *
     * @param consumer The callback to execute
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public static void whenLoaded(@NotNull Consumer<MultiverseNetherPortalsApi> consumer) {
        if (instance != null) {
            consumer.accept(instance);
        } else {
            WHEN_LOADED_CALLBACKS.add(consumer);
        }
    }

    /**
     * Checks whether the Multiverse-NetherPortals API has been initialized.
     *
     * @return {@code true} if the API has been initialized, otherwise {@code false}
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public static boolean isLoaded() {
        return instance != null;
    }

    /**
     * Gets the Multiverse-NetherPortals API.
     *
     * @return The Multiverse-NetherPortals API
     * @throws IllegalStateException if Multiverse-NetherPortals has not been initialized
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    @NotNull
    public static MultiverseNetherPortalsApi get() {
        if (instance == null) {
            throw new IllegalStateException("MultiverseNetherPortalsApi has not been initialized!");
        }
        return instance;
    }

    private final PluginServiceLocator serviceLocator;

    private MultiverseNetherPortalsApi(@NotNull PluginServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * Gets the NetherPortalsConfig instance.
     *
     * @return The NetherPortalsConfig instance
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    @NotNull
    public NetherPortalsConfig getNetherPortalsConfig() {
        return Objects.requireNonNull(serviceLocator.getActiveService(NetherPortalsConfig.class));
    }

    /**
     * Gets the LinksManager instance.
     *
     * @return The LinksManager instance
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    @NotNull
    public LinksManager getLinksManager() {
        return Objects.requireNonNull(serviceLocator.getActiveService(LinksManager.class));
    }

    /**
     * Gets the CustomPortalsHandler instance.
     *
     * @return The CustomPortalsHandler instance
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    @NotNull
    public CustomPortalsHandler getCustomPortalsHandler() {
        return Objects.requireNonNull(serviceLocator.getActiveService(CustomPortalsHandler.class));
    }

    /**
     * Gets the Multiverse-NetherPortals PluginServiceLocator.
     * <br/>
     * You can use this to hook into the dependency injection system used by Multiverse-NetherPortals.
     *
     * @return The Multiverse-NetherPortals PluginServiceLocator
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    @NotNull
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
