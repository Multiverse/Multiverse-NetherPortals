package org.mvplugins.multiverse.netherportals.links

import org.bukkit.PortalType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldLinkTypeTest {

    @Test
    fun `Link types map to and from portal types`() {
        assertEquals(PortalType.NETHER, WorldLinkType.NETHER.toPortalType())
        assertEquals(PortalType.ENDER, WorldLinkType.END.toPortalType())
        assertEquals(WorldLinkType.NETHER, WorldLinkType.fromPortalType(PortalType.NETHER).get())
        assertEquals(WorldLinkType.END, WorldLinkType.fromPortalType(PortalType.ENDER).get())
        assertTrue(WorldLinkType.fromPortalType(PortalType.CUSTOM).isEmpty)
    }

    @Test
    fun `Link types provide lowercase config keys`() {
        assertEquals("nether", WorldLinkType.NETHER.configKey)
        assertEquals("end", WorldLinkType.END.configKey)
    }
}
