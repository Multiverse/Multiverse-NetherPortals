package org.mvplugins.multiverse.netherportals.utils.customportals;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Checker callback for your plugin to determine if the given location is a portal handled by your plugin.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
@FunctionalInterface
public interface CustomPortalsHandleCheck {

    /**
     * Checks if the given portal location is handled by this plugin. When you return true, Multiverse-NetherPortals
     * will not run any of its portal linking logic on portal events.
     *
     * @param entity            The entity that is using the portal, if available.
     * @param portalLocation    The location of the portal being used.
     * @return true if your plugin is handling the portal at the given location, false otherwise.
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    boolean isHandledByThisPlugin(@Nullable Entity entity, @NotNull Location portalLocation);
}
