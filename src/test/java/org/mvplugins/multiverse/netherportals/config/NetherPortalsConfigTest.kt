package org.mvplugins.multiverse.netherportals.config

import org.bukkit.configuration.file.YamlConfiguration
import org.mvplugins.multiverse.netherportals.TestWithMockBukkit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetherPortalsConfigTest : TestWithMockBukkit() {

    private lateinit var config: NetherPortalsConfig

    @BeforeTest
    fun setUp() {
        config = serviceLocator.getService(NetherPortalsConfig::class.java)
    }

    @Test
    fun `Config is loaded`() {
        assertTrue(config.isLoaded)
    }

    @Test
    fun `Legacy config keys are migrated`() {
        writeResourceFileToPluginDataFolder("/configs/legacy_config.yml", NetherPortalsConfig.CONFIG_FILENAME)

        assertTrue(config.load().isSuccess)
        assertEquals(false, config.isTeleportingEntities)
        assertEquals(false, config.isSendingDisabledPortalMessage)
        assertEquals(false, config.isSendingNoDestinationMessage)
        assertEquals(false, config.isEndPlatformDropBlocks)
        assertTrue(config.save().isSuccess)

        val configFile = multiverseNetherPortals.dataFolder.resolve(NetherPortalsConfig.CONFIG_FILENAME)
        val migratedConfig = YamlConfiguration.loadConfiguration(configFile)
        assertNull(migratedConfig.get("teleport_entities"))
        assertNull(migratedConfig.get("send_disabled_portal_message"))
        assertNull(migratedConfig.get("send_no_destination_message"))
        assertNull(migratedConfig.get("end_platform_drop_blocks"))
        assertEquals(false, migratedConfig.getBoolean("teleport-entities"))
        assertEquals(false, migratedConfig.getBoolean("send-disabled-portal-message"))
        assertEquals(false, migratedConfig.getBoolean("send-no-destination-message"))
        assertEquals(false, migratedConfig.getBoolean("end-platform-drop-blocks"))
    }
}
