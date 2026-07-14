package org.mvplugins.multiverse.netherportals.links;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig;

/**
 * Manages the links.yml file.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
@Service
public final class LinksManager {

    public static final String LINKS_FILENAME = "links.yml";

    private static final String LEGACY_WORLDS_PATH = "worlds";
    private static final String LEGACY_PORTAL_GOES_TO = "portalgoesto";

    private final SortedMap<String, WorldLink> worldLinkMap;
    private final File linksConfigFile;
    private final NetherPortalsConfig config;

    private YamlConfiguration linksConfig;

    @Inject
    LinksManager(@NotNull MultiverseNetherPortals plugin, @NotNull NetherPortalsConfig config) {
        worldLinkMap = new TreeMap<>();
        linksConfigFile = plugin.getDataFolder().toPath().resolve(LINKS_FILENAME).toFile();
        this.config = config;
    }

    /**
     * Loads links.yml and imports any legacy links from config.yml.
     *
     * @return The result of the load operation.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> load() {
        return Try.run(() -> {
                    loadLinksYmlFile();
                    parseLinks();
                    migrateLegacyConfigLinks();
                })
                .onFailure(e -> Logging.severe("Failed to load links.yml file: %s", e.getMessage()));
    }

    private void loadLinksYmlFile() throws IOException, InvalidConfigurationException {
        if (!linksConfigFile.exists() && !linksConfigFile.createNewFile()) {
            throw new IllegalStateException("Could not create links.yml config file");
        }
        linksConfig = new YamlConfiguration();
        linksConfig.load(linksConfigFile);
    }

    private void parseLinks() {
        Collection<String> allWorldsInConfig = linksConfig.getKeys(false).stream()
                .map(this::decodeConfigKey)
                .toList();

        SortedMap<String, WorldLink> parsedWorldLinks = new TreeMap<>();
        for (String worldName : allWorldsInConfig) {
            ConfigurationSection linkSection = getLinkConfigSection(worldName);
            WorldLink worldLink = new WorldLink(worldName, linkSection);
            if (worldLink.hasLinks()) {
                parsedWorldLinks.put(worldName, worldLink);
            }
        }
        worldLinkMap.clear();
        worldLinkMap.putAll(parsedWorldLinks);
    }

    /**
     * Checks whether links.yml has been loaded.
     *
     * @return Whether the manager is loaded.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isLoaded() {
        return linksConfig != null;
    }

    /**
     * Saves all world links to links.yml.
     *
     * @return The result of the save operation.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> save() {
        return Try.run(() -> {
                    if (!isLoaded()) {
                        throw new IllegalStateException("LinksManager is not loaded!");
                    }
                    linksConfig = new YamlConfiguration();
                    worldLinkMap.forEach((worldName, worldLink) ->
                            linksConfig.set(encodeConfigKey(worldName), worldLink.save()));
                    linksConfig.save(linksConfigFile);
                })
                .onFailure(e -> Logging.severe("Failed to save links.yml file: %s", e.getMessage()));
    }

    /**
     * Gets the complete link configuration for a source world.
     *
     * @param worldName The source world name.
     * @return The world link, or an empty option when none exists.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull Option<WorldLink> getWorldLink(@NotNull String worldName) {
        return Option.of(worldLinkMap.get(worldName));
    }

    /**
     * Gets a destination for a source world and link type.
     *
     * @param worldName The source world name.
     * @param worldLinkType The link type.
     * @return The destination world, or an empty option when unset.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull Option<String> getWorldLink(
            @NotNull String worldName,
            @NotNull WorldLinkType worldLinkType) {
        return getWorldLink(worldName)
                .flatMap(worldLink -> worldLink.getLinkTo(worldLinkType));
    }

    /**
     * Gets all configured world links.
     *
     * @return An unmodifiable collection of world links.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull Collection<WorldLink> getWorldLinks() {
        return Collections.unmodifiableCollection(worldLinkMap.values());
    }

    /**
     * Gets all source-to-destination mappings for a link type.
     *
     * @param worldLinkType The link type.
     * @return The configured source-to-destination mappings.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public @NotNull Map<String, String> getLinksForType(@NotNull WorldLinkType worldLinkType) {
        Map<String, String> links = new LinkedHashMap<>();
        worldLinkMap.forEach((worldName, worldLink) -> worldLink.getLinkTo(worldLinkType)
                .peek(destination -> links.put(worldName, destination)));
        return links;
    }

    /**
     * Adds or replaces a world link in memory.
     *
     * @param fromWorld The source world name.
     * @param toWorld The destination world name.
     * @param worldLinkType The link type.
     * @return Whether the link was updated.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean addWorldLink(
            @NotNull String fromWorld,
            @NotNull String toWorld,
            @NotNull WorldLinkType worldLinkType) {
        WorldLink worldLink = worldLinkMap.computeIfAbsent(fromWorld,
                WorldLink::new);
        worldLink.setLinkTo(worldLinkType, toWorld);
        return true;
    }

    /**
     * Adds or replaces a world link in memory.
     *
     * @param fromWorld The source world.
     * @param toWorld The destination world.
     * @param worldLinkType The link type.
     * @return Whether the link was updated.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean addWorldLink(
            @NotNull MultiverseWorld fromWorld,
            @NotNull MultiverseWorld toWorld,
            @NotNull WorldLinkType worldLinkType) {
        return addWorldLink(fromWorld.getName(), toWorld.getName(), worldLinkType);
    }

    /**
     * Removes a world link from memory.
     *
     * @param fromWorld The source world name.
     * @param worldLinkType The link type.
     * @return Whether the link existed and was removed.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean removeWorldLink(
            @NotNull String fromWorld,
            @NotNull WorldLinkType worldLinkType) {
        WorldLink worldLink = worldLinkMap.get(fromWorld);
        if (worldLink == null || worldLink.getLinkTo(worldLinkType).isEmpty()) {
            return false;
        }
        worldLink.removeLinkTo(worldLinkType);
        if (!worldLink.hasLinks()) {
            worldLinkMap.remove(fromWorld);
        }
        return true;
    }

    /**
     * Removes a world link from memory.
     *
     * @param fromWorld The source world.
     * @param worldLinkType The link type.
     * @return Whether the link existed and was removed.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean removeWorldLink(
            @NotNull MultiverseWorld fromWorld,
            @NotNull WorldLinkType worldLinkType) {
        return removeWorldLink(fromWorld.getName(), worldLinkType);
    }

    private void migrateLegacyConfigLinks() {
        ConfigurationSection worldsSection = config.getRawConfig().getConfigurationSection(LEGACY_WORLDS_PATH);
        if (worldsSection == null) {
            return;
        }

        int migratedLinks = migrateLegacyWorldSections(worldsSection.getValues(false), "");
        if (migratedLinks > 0) {
            Logging.info("Migrated %s portal links from config.yml to links.yml.", migratedLinks);
        }
    }

    private int migrateLegacyWorldSections(Map<String, Object> worldSections, String parentPath) {
        int migratedLinks = 0;
        for (Map.Entry<String, Object> entry : worldSections.entrySet()) {
            String worldName = parentPath + entry.getKey();
            Map<String, Object> world = asMap(entry.getValue());
            Map<String, Object> portalGoesTo = asMap(world.get(LEGACY_PORTAL_GOES_TO));
            if (portalGoesTo.isEmpty()) {
                migratedLinks += migrateLegacyWorldSections(world, worldName + ".");
                continue;
            }

            String netherLink = asString(portalGoesTo.get(WorldLinkType.NETHER.toPortalType().name()));
            String endLink = asString(portalGoesTo.get(WorldLinkType.END.toPortalType().name()));
            if (endLink == null) {
                endLink = asString(portalGoesTo.get(WorldLinkType.END.name()));
            }
            if (netherLink == null && endLink == null) {
                continue;
            }

            WorldLink worldLink = worldLinkMap.computeIfAbsent(worldName,
                    WorldLink::new);
            if (worldLink.getLinkTo(WorldLinkType.NETHER).isEmpty() && netherLink != null) {
                worldLink.setLinkTo(WorldLinkType.NETHER, netherLink);
                migratedLinks++;
            }
            if (worldLink.getLinkTo(WorldLinkType.END).isEmpty() && endLink != null) {
                worldLink.setLinkTo(WorldLinkType.END, endLink);
                migratedLinks++;
            }
        }
        return migratedLinks;
    }

    private @NotNull ConfigurationSection getLinkConfigSection(@NotNull String worldName) {
        String encodedWorldName = encodeConfigKey(worldName);
        ConfigurationSection section = linksConfig.getConfigurationSection(encodedWorldName);
        return section == null ? linksConfig.createSection(encodedWorldName) : section;
    }

    private String encodeConfigKey(@NotNull String worldName) {
        return worldName.replace(".", "[dot]");
    }

    private String decodeConfigKey(@NotNull String worldName) {
        return worldName.replace("[dot]", ".");
    }

    private static Map<String, Object> asMap(Object value) {
        Map<?, ?> source;
        if (value instanceof ConfigurationSection section) {
            source = section.getValues(false);
        } else if (value instanceof Map<?, ?> map) {
            source = map;
        } else {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, childValue) -> result.put(Objects.toString(key), childValue));
        return result;
    }

    private static @Nullable String asString(Object value) {
        return value instanceof String stringValue ? stringValue : null;
    }
}
