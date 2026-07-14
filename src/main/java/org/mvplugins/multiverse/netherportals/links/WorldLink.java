package org.mvplugins.multiverse.netherportals.links;

import java.util.EnumMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;

/**
 * Stores the Nether and End portal destinations configured for a world.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
public final class WorldLink {

    private final String from;
    private final EnumMap<WorldLinkType, String> links;

    WorldLink(@NotNull String from, @NotNull ConfigurationSection configurationSection) {
        this.from = from;
        this.links = new EnumMap<>(WorldLinkType.class);
        load(configurationSection);
    }

    /**
     * Creates an empty set of portal links for a world.
     *
     * @param from The source world name.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public WorldLink(@NotNull String from) {
        this.from = from;
        this.links = new EnumMap<>(WorldLinkType.class);
    }

    void load(@NotNull ConfigurationSection configurationSection) {
        links.clear();
        for (WorldLinkType worldLinkType : WorldLinkType.values()) {
            Option.of(configurationSection.getString(worldLinkType.getConfigKey()))
                    .peek(link -> links.put(worldLinkType, link));
        }
    }

    @NotNull ConfigurationSection save() {
        MemoryConfiguration configuration = new MemoryConfiguration();
        links.forEach((worldLinkType, link) ->
                configuration.set(worldLinkType.getConfigKey(), link));
        return configuration;
    }

    /**
     * Gets the source world name.
     *
     * @return The source world name.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull String getFrom() {
        return from;
    }

    /**
     * Gets the destination for a link type.
     *
     * @param worldLinkType The link type.
     * @return The configured destination, or an empty option when unset.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull Option<String> getLinkTo(@NotNull WorldLinkType worldLinkType) {
        return Option.of(links.get(worldLinkType));
    }

    /**
     * Sets the destination for a link type.
     *
     * @param worldLinkType The link type.
     * @param link The destination world name.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public void setLinkTo(@NotNull WorldLinkType worldLinkType, @NotNull String link) {
        links.put(worldLinkType, link);
    }

    /**
     * Removes the destination for a link type.
     *
     * @param worldLinkType The link type.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public void removeLinkTo(@NotNull WorldLinkType worldLinkType) {
        links.remove(worldLinkType);
    }

    /**
     * Checks whether this world has any configured destinations.
     *
     * @return Whether at least one link is configured.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean hasLinks() {
        return !links.isEmpty();
    }
}
