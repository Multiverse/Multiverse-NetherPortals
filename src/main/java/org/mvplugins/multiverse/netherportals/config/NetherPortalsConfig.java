package org.mvplugins.multiverse.netherportals.config;

import java.nio.file.Path;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.handle.CommentedConfigurationHandle;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.migration.VersionMigrator;
import org.mvplugins.multiverse.core.config.migration.action.MoveMigratorAction;
import org.mvplugins.multiverse.core.config.migration.action.SetMigratorAction;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

/**
 * Provides typed access to the Multiverse-NetherPortals configuration.
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
@Service
public final class NetherPortalsConfig {

    public static final String CONFIG_FILENAME = "config.yml";

    private final NetherPortalsConfigNodes configNodes;
    private final CommentedConfigurationHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    @Inject
    NetherPortalsConfig(
            @NotNull MultiverseNetherPortals plugin,
            @NotNull NetherPortalsConfigNodes configNodes) {
        Path configPath = Path.of(plugin.getDataFolder().getPath(), CONFIG_FILENAME);
        this.configNodes = configNodes;
        this.configHandle = CommentedConfigurationHandle.builder(configPath, configNodes.getNodes())
                .logger(Logging.getLogger())
                .migrator(ConfigMigrator.builder(configNodes.version)
                        .addVersionMigrator(VersionMigrator.builder(5.1)
                                .addAction(SetMigratorAction.of("handle-end-exit-respawn", false))
                                .addAction(MoveMigratorAction.of("teleport_entities", "teleport-entities"))
                                .addAction(MoveMigratorAction.of(
                                        "send_disabled_portal_message", "send-disabled-portal-message"))
                                .addAction(MoveMigratorAction.of(
                                        "send_no_destination_message", "send-no-destination-message"))
                                .addAction(MoveMigratorAction.of(
                                        "end_platform_drop_blocks", "end-platform-drop-blocks"))
                                .build())
                        .build())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(configHandle);
    }

    /**
     * Loads and migrates config.yml.
     *
     * @return The result of the load operation.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> load() {
        return configHandle.load()
                .onFailure(e -> Logging.severe("Failed to load Multiverse-NetherPortals config.yml!", e));
    }

    /**
     * Checks whether config.yml has been loaded.
     *
     * @return Whether the configuration is loaded.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isLoaded() {
        return configHandle.isLoaded();
    }

    /**
     * Saves config.yml.
     *
     * @return The result of the save operation.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> save() {
        return configHandle.save()
                .onFailure(e -> Logging.severe("Failed to save Multiverse-NetherPortals config.yml!", e));
    }

    /**
     * Gets the string property handle for configuration commands.
     *
     * @return The string property handle.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    /**
     * Sets the prefix used to identify Nether worlds.
     *
     * @param netherPrefix The Nether world prefix.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setNetherPrefix(String netherPrefix) {
        return configHandle.set(configNodes.netherPrefix, netherPrefix);
    }

    /**
     * Gets the prefix used to identify Nether worlds.
     *
     * @return The Nether world prefix.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public String getNetherPrefix() {
        return configHandle.get(configNodes.netherPrefix);
    }

    /**
     * Sets the suffix used to identify Nether worlds.
     *
     * @param netherSuffix The Nether world suffix.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setNetherSuffix(String netherSuffix) {
        return configHandle.set(configNodes.netherSuffix, netherSuffix);
    }

    /**
     * Gets the suffix used to identify Nether worlds.
     *
     * @return The Nether world suffix.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public String getNetherSuffix() {
        return configHandle.get(configNodes.netherSuffix);
    }

    /**
     * Sets the prefix used to identify End worlds.
     *
     * @param endPrefix The End world prefix.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setEndPrefix(String endPrefix) {
        return configHandle.set(configNodes.endPrefix, endPrefix);
    }

    /**
     * Gets the prefix used to identify End worlds.
     *
     * @return The End world prefix.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public String getEndPrefix() {
        return configHandle.get(configNodes.endPrefix);
    }

    /**
     * Sets the suffix used to identify End worlds.
     *
     * @param endSuffix The End world suffix.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setEndSuffix(String endSuffix) {
        return configHandle.set(configNodes.endSuffix, endSuffix);
    }

    /**
     * Gets the suffix used to identify End worlds.
     *
     * @return The End world suffix.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public String getEndSuffix() {
        return configHandle.get(configNodes.endSuffix);
    }

    /**
     * Checks whether Multiverse should override end portal exits and respawn players in the linked world's spawn.
     *
     * @return Whether end exit respawn is enabled.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean shouldHandleEndExitRespawn() {
        return configHandle.get(configNodes.handleEndExitRespawn);
    }

    /**
     * Sets whether Multiverse should override end portal exits and respawn players in the linked world's spawn.
     *
     * @param handleEndExitRespawn Whether end exit respawn should be enabled.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setHandleEndExitRespawn(boolean handleEndExitRespawn) {
        return configHandle.set(configNodes.handleEndExitRespawn, handleEndExitRespawn);
    }

    /**
     * Checks whether unavailable portals bounce players back.
     *
     * @return Whether bounceback is enabled.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isUsingBounceBack() {
        return configHandle.get(configNodes.usingBounceBack);
    }

    /**
     * Sets whether unavailable portals bounce players back.
     *
     * @param usingBounceBack Whether bounceback should be enabled.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setUsingBounceBack(boolean usingBounceBack) {
        return configHandle.set(configNodes.usingBounceBack, usingBounceBack);
    }

    /**
     * Checks whether non-player entities can use portals.
     *
     * @return Whether entity teleportation is enabled.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isTeleportingEntities() {
        return configHandle.get(configNodes.teleportingEntities);
    }

    /**
     * Sets whether non-player entities can use portals.
     *
     * @param teleportingEntities Whether entity teleportation should be enabled.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setTeleportingEntities(boolean teleportingEntities) {
        return configHandle.set(configNodes.teleportingEntities, teleportingEntities);
    }

    /**
     * Checks whether players are notified about disabled portals.
     *
     * @return Whether disabled portal messages are enabled.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isSendingDisabledPortalMessage() {
        return configHandle.get(configNodes.sendingDisabledPortalMessage);
    }

    /**
     * Sets whether players are notified about disabled portals.
     *
     * @param sendingDisabledPortalMessage Whether disabled portal messages should be enabled.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setSendingDisabledPortalMessage(boolean sendingDisabledPortalMessage) {
        return configHandle.set(configNodes.sendingDisabledPortalMessage, sendingDisabledPortalMessage);
    }

    /**
     * Checks whether players are notified when portals have no destination.
     *
     * @return Whether no-destination messages are enabled.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isSendingNoDestinationMessage() {
        return configHandle.get(configNodes.sendingNoDestinationMessage);
    }

    /**
     * Sets whether players are notified when portals have no destination.
     *
     * @param sendingNoDestinationMessage Whether no-destination messages should be enabled.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setSendingNoDestinationMessage(boolean sendingNoDestinationMessage) {
        return configHandle.set(configNodes.sendingNoDestinationMessage, sendingNoDestinationMessage);
    }

    /**
     * Checks whether replaced End platform blocks drop as items.
     *
     * @return Whether replaced blocks should drop as items.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean isEndPlatformDropBlocks() {
        return configHandle.get(configNodes.endPlatformDropBlocks);
    }

    /**
     * Sets whether replaced End platform blocks drop as items.
     *
     * @param endPlatformDropBlocks Whether replaced blocks should drop as items.
     * @return The result of the update.
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setEndPlatformDropBlocks(boolean endPlatformDropBlocks) {
        return configHandle.set(configNodes.endPlatformDropBlocks, endPlatformDropBlocks);
    }

    /**
     * Gets the raw configuration while legacy links are being migrated.
     *
     * @return The underlying configuration.
     * @since 5.1
     */
    @ApiStatus.Internal
    @ApiStatus.AvailableSince("5.1")
    public FileConfiguration getRawConfig() {
        return configHandle.getConfig();
    }
}
