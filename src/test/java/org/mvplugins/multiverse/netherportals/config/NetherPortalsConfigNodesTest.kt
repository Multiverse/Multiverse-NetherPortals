package org.mvplugins.multiverse.netherportals.config

import org.bukkit.configuration.file.YamlConfiguration
import org.mvplugins.multiverse.netherportals.TestWithMockBukkit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetherPortalsConfigNodesTest : TestWithMockBukkit() {

    private lateinit var config: NetherPortalsConfig

    @BeforeTest
    fun setUp() {
        config = serviceLocator.getService(NetherPortalsConfig::class.java)
    }

    @Test
    fun `Fresh config uses current node names and defaults`() {
        assertTrue(config.save().isSuccess)

        val configFile = multiverseNetherPortals.dataFolder.resolve(NetherPortalsConfig.CONFIG_FILENAME)
        val yaml = YamlConfiguration.loadConfiguration(configFile)
        assertEquals(true, yaml.getBoolean("bounceback"))
        assertEquals(true, yaml.getBoolean("teleport-entities"))
        assertEquals(true, yaml.getBoolean("send-disabled-portal-message"))
        assertEquals(true, yaml.getBoolean("send-no-destination-message"))
        assertEquals(true, yaml.getBoolean("end-platform-drop-blocks"))
        assertNull(yaml.get("teleport_entities"))
        assertNull(yaml.get("send_disabled_portal_message"))
    }
}
