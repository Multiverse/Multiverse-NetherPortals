package org.mvplugins.multiverse.netherportals.utils.customportals;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.MultiversePortalsApi;

import java.util.ArrayList;
import java.util.List;

/**
 * A handler for managing custom portal conflicting with Multiverse-NetherPortals. Some portal plugins want to use
 * vanilla portal event to handle their own custom logic. To ensure Multiverse-NetherPortals logic does not interfere
 * with these plugins, you can register a custom portal handle check to tell Multiverse-NetherPortals to let your plugin
 * handle the portal logic instead.
 * <br />
 * One example is Multiverse-Portals, which has its own portal destination logic which should override
 * Multiverse-NetherPortals vanilla linkings.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
@Service
public class CustomPortalsHandler {

    private final List<CustomPortalsHandleCheck> handleChecks;

    @Inject
    CustomPortalsHandler() {
        this.handleChecks = new ArrayList<>();
        registerMultiversePortalsCheck();
    }

    private void registerMultiversePortalsCheck() {
        registerHandleCheck((entity, portalLocation) -> {
            if (!Bukkit.getPluginManager().isPluginEnabled("Multiverse-Portals")) {
                return false;
            }
            return Try.of(() -> MultiversePortalsApi.get().getPortalManager())
                    .map(portalManager -> portalManager.isPortal(portalLocation))
                    .onFailure(throwable ->
                            Logging.warning("Error while checking if portal is handled by Multiverse-Portals: %s",
                                    throwable.getMessage()))
                    .getOrElse(false);
        });
    }

    /**
     * Registers a custom portal handle check.
     *
     * @param check The check handle to register.
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public void registerHandleCheck(CustomPortalsHandleCheck check) {
        this.handleChecks.add(check);
    }

    /**
     * Unregisters a custom portal handle check.
     *
     * @param check The check handle to unregister.
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public void unregisterHandleCheck(CustomPortalsHandleCheck check) {
        this.handleChecks.remove(check);
    }

    /**
     * Checks if a portal at the given location is handled by any of the registered custom portal handlers.
     *
     * @param entity The entity that might be using the portal.
     * @param portalLocation The location of the portal.
     * @return true if the portal is handled by a custom portal handler, false otherwise.
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isHandledByCustomPortals(@Nullable Entity entity, @NotNull Location portalLocation) {
        return handleChecks.stream()
                .anyMatch(check ->
                        Try.of(() -> check.isHandledByThisPlugin(entity, portalLocation))
                                .onFailure(throwable ->
                                        Logging.warning("Error while checking if portal is handled by custom portals: %s",
                                                throwable.getMessage()))
                                .getOrElse(false));
    }
}
