package org.mvplugins.multiverse.netherportals

import org.bukkit.PortalType
import org.mvplugins.multiverse.netherportals.links.LinksManager
import org.mvplugins.multiverse.netherportals.links.WorldLinkType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiverseNetherPortalsTest : TestWithMockBukkit() {

    private lateinit var linksManager: LinksManager

    @BeforeTest
    fun setUp() {
        linksManager = serviceLocator.getService(LinksManager::class.java)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Deprecated link methods save immediately`() {
        assertTrue(multiverseNetherPortals.addWorldLink("world", "world_nether", PortalType.NETHER))
        assertTrue(linksManager.load().isSuccess)
        assertEquals("world_nether", linksManager.getWorldLink("world", WorldLinkType.NETHER).get())

        assertTrue(multiverseNetherPortals.removeWorldLink("world", "world_nether", PortalType.NETHER))
        assertTrue(linksManager.load().isSuccess)
        assertTrue(linksManager.getWorldLink("world", WorldLinkType.NETHER).isEmpty)
    }
}
