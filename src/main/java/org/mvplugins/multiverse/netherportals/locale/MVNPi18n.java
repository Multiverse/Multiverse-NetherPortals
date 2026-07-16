package org.mvplugins.multiverse.netherportals.locale;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
import org.mvplugins.multiverse.external.acf.locales.MessageKey;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

/**
 * All translation keys for Multiverse-NetherPortals.
 */
@ApiStatus.AvailableSince("5.1")
@ApiStatus.Internal
public enum MVNPi18n implements MessageKeyProvider {
    // BEGIN CHECKSTYLE-SUPPRESSION: JavadocVariable

    // /mvnp link
    LINK_DESCRIPTION,
    LINK_TYPE_DESCRIPTION,
    LINK_FROMWORLD_DESCRIPTION,
    LINK_TOWORLD_DESCRIPTION,
    LINK_FAILED,
    LINK_DISABLED,
    LINK_SUCCESS,

    // /mvnp unlink
    UNLINK_DESCRIPTION,
    UNLINK_TYPE_DESCRIPTION,
    UNLINK_FROMWORLD_DESCRIPTION,
    UNLINK_NOTLINKED,
    UNLINK_BIDIRECTIONAL_MISMATCH,
    UNLINK_FAILED,
    UNLINK_ENABLED,
    UNLINK_SUCCESS,

    // /mvnp list
    LIST_DESCRIPTION,
    LIST_TYPE_DESCRIPTION,
    LIST_HEADER_ALL,
    LIST_HEADER,
    LIST_NOCONTENT_ALL,
    LIST_NOCONTENT,

    // Portal use
    PORTAL_DISABLED,
    PORTAL_NODESTINATION,
    PORTAL_AUTOLINKEDWORLD_NOTFOUND;

    // END CHECKSTYLE-SUPPRESSION: JavadocVariable

    private final MessageKey key = MessageKey.of("mv-netherportals." + this.name().replace('_', '.')
            .toLowerCase(Locale.ENGLISH));

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageKey getMessageKey() {
        return this.key;
    }

    /**
     * Creates a message with a non-localized fallback and replacements.
     *
     * @param nonLocalizedMessage The non-localized fallback message.
     * @param replacements The replacements.
     * @return A new localizable message.
     */
    @NotNull public Message bundle(
            @NotNull String nonLocalizedMessage,
            @NotNull MessageReplacement... replacements) {
        return Message.of(this, nonLocalizedMessage, replacements);
    }
}
