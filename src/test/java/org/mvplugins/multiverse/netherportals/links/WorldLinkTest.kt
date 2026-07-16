package org.mvplugins.multiverse.netherportals.links

import org.bukkit.configuration.MemoryConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorldLinkTest {

    @Test
    fun `Links can be added retrieved and removed by type`() {
        val worldLink = WorldLink("world")

        assertEquals("world", worldLink.from)
        assertFalse(worldLink.hasLinks())
        assertTrue(worldLink.getLinkTo(WorldLinkType.NETHER).isEmpty)

        worldLink.setLinkTo(WorldLinkType.NETHER, "world_nether")
        worldLink.setLinkTo(WorldLinkType.END, "world_the_end")
        assertEquals("world_nether", worldLink.getLinkTo(WorldLinkType.NETHER).get())
        assertEquals("world_the_end", worldLink.getLinkTo(WorldLinkType.END).get())
        assertTrue(worldLink.hasLinks())

        worldLink.removeLinkTo(WorldLinkType.NETHER)
        assertTrue(worldLink.getLinkTo(WorldLinkType.NETHER).isEmpty)
        assertTrue(worldLink.hasLinks())
        worldLink.removeLinkTo(WorldLinkType.END)
        assertFalse(worldLink.hasLinks())
    }

    @Test
    fun `Links load from and save to configuration sections`() {
        val section = MemoryConfiguration()
        section.set("nether", "world_nether")
        section.set("end", "world_the_end")

        val worldLink = WorldLink("world", section)
        assertEquals("world_nether", worldLink.getLinkTo(WorldLinkType.NETHER).get())
        assertEquals("world_the_end", worldLink.getLinkTo(WorldLinkType.END).get())

        val saved = worldLink.save()
        assertEquals("world_nether", saved.getString("nether"))
        assertEquals("world_the_end", saved.getString("end"))
    }
}
