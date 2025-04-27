import kotlin.test.Test
import kotlin.test.assertNotNull

class MockBukkitTest : TestWithMockBukkit() {

    @Test
    fun `MockBukkit loads the plugin`() {
        assertNotNull(multiverseNetherPortals)
    }
}