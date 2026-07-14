package org.mvplugins.multiverse.netherportals.links;

import java.util.Locale;

import org.bukkit.PortalType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.locale.message.LocalizableMessage;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Option;

/**
 * The supported types of world portal links.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
public enum WorldLinkType implements LocalizableMessage {

    NETHER(PortalType.NETHER),
    END(PortalType.ENDER);

    private final PortalType portalType;

    WorldLinkType(PortalType portalType) {
        this.portalType = portalType;
    }

    /**
     * Gets the corresponding Bukkit portal type.
     *
     * @return The corresponding portal type.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull PortalType toPortalType() {
        return portalType;
    }

    /**
     * Gets the key used for this link type in links.yml.
     *
     * @return The lowercase configuration key.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull String getConfigKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the world link type represented by a Bukkit portal type.
     *
     * @param portalType The Bukkit portal type.
     * @return The matching world link type, or an empty option when unsupported.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public static @NotNull Option<WorldLinkType> fromPortalType(@NotNull PortalType portalType) {
        for (WorldLinkType worldLinkType : values()) {
            if (worldLinkType.portalType == portalType) {
                return Option.of(worldLinkType);
            }
        }
        return Option.none();
    }

    @Override
    public @Nullable Message getLocalizableMessage() {
        return Message.of(getConfigKey());
    }
}
