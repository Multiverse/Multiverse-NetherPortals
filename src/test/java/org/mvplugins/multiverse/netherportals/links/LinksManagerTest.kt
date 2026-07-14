package org.mvplugins.multiverse.netherportals.links

import org.bukkit.configuration.file.YamlConfiguration
import org.mvplugins.multiverse.netherportals.TestWithMockBukkit
import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LinksManagerTest : TestWithMockBukkit() {

    private lateinit var config: NetherPortalsConfig
    private lateinit var linksManager: LinksManager

    @BeforeTest
    fun setUp() {
        config = serviceLocator.getService(NetherPortalsConfig::class.java)
        linksManager = serviceLocator.getService(LinksManager::class.java)
    }

    @Test
    fun `Adding and removing links requires an explicit save`() {
        val linksFile = multiverseNetherPortals.dataFolder.resolve(LinksManager.LINKS_FILENAME)

        assertTrue(linksManager.addWorldLink("world", "world_nether", WorldLinkType.NETHER))
        assertEquals("world_nether", linksManager.getWorldLink("world", WorldLinkType.NETHER).get())
        assertNull(YamlConfiguration.loadConfiguration(linksFile).get("world.nether"))

        assertTrue(linksManager.save().isSuccess)
        assertEquals(
            "world_nether",
            YamlConfiguration.loadConfiguration(linksFile).getString("world.nether")
        )

        assertTrue(linksManager.removeWorldLink("world", WorldLinkType.NETHER))
        assertTrue(linksManager.getWorldLink("world", WorldLinkType.NETHER).isEmpty)
        assertEquals(
            "world_nether",
            YamlConfiguration.loadConfiguration(linksFile).getString("world.nether")
        )

        assertTrue(linksManager.save().isSuccess)
        assertNull(YamlConfiguration.loadConfiguration(linksFile).get("world"))
    }

    @Test
    fun `Links persist across reload`() {
        assertTrue(linksManager.addWorldLink("world", "world_nether", WorldLinkType.NETHER))
        assertTrue(linksManager.save().isSuccess)
        assertTrue(linksManager.load().isSuccess)

        val worldLink = linksManager.getWorldLink("world").get()
        assertEquals("world", worldLink.from)
        assertEquals("world_nether", worldLink.getLinkTo(WorldLinkType.NETHER).get())
        assertTrue(worldLink.getLinkTo(WorldLinkType.END).isEmpty)
        assertTrue(linksManager.getWorldLink("missing").isEmpty)
    }

    @Test
    fun `Legacy config world links migrate to links yml`() {
        writeResourceFileToPluginDataFolder("/configs/legacy_config.yml", NetherPortalsConfig.CONFIG_FILENAME)

        assertTrue(config.load().isSuccess)
        assertTrue(linksManager.load().isSuccess)
        assertEquals("world_nether", linksManager.getWorldLink("world", WorldLinkType.NETHER).get())
        assertEquals("world_the_end", linksManager.getWorldLink("world", WorldLinkType.END).get())
        assertEquals("other_world_the_end", linksManager.getWorldLink("other_world", WorldLinkType.END).get())
        assertEquals(
            "example.world_nether",
            linksManager.getWorldLink("example.world", WorldLinkType.NETHER).get()
        )

        assertTrue(linksManager.save().isSuccess)
        assertTrue(config.save().isSuccess)

        val configFile = multiverseNetherPortals.dataFolder.resolve(NetherPortalsConfig.CONFIG_FILENAME)
        assertNull(YamlConfiguration.loadConfiguration(configFile).get("worlds"))

        val linksFile = multiverseNetherPortals.dataFolder.resolve(LinksManager.LINKS_FILENAME)
        val linksYaml = YamlConfiguration.loadConfiguration(linksFile)
        assertEquals("world_nether", linksYaml.getString("world.nether"))
        assertEquals("world_the_end", linksYaml.getString("world.end"))
        assertEquals("other_world_the_end", linksYaml.getString("other_world.end"))
        assertEquals("example.world_nether", linksYaml.getString("example[dot]world.nether"))
    }

    @Test
    fun `Dots in world names are encoded and decoded`() {
        assertTrue(linksManager.addWorldLink(
            "example.world",
            "example.world_nether",
            WorldLinkType.NETHER
        ))
        assertTrue(linksManager.save().isSuccess)
        assertTrue(linksManager.load().isSuccess)
        assertEquals(
            "example.world_nether",
            linksManager.getWorldLink("example.world", WorldLinkType.NETHER).get()
        )

        val linksFile = multiverseNetherPortals.dataFolder.resolve(LinksManager.LINKS_FILENAME)
        val yaml = YamlConfiguration.loadConfiguration(linksFile)
        assertEquals("example.world_nether", yaml.getString("example[dot]world.nether"))
    }
}
