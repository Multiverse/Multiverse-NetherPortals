import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mvplugins.multiverse.core.MultiverseCore
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class TestWithMockBukkit {

    protected lateinit var server: ServerMock
    protected lateinit var multiverseCore: MultiverseCore
    protected lateinit var multiverseNetherPortals: MultiverseNetherPortals

    @BeforeTest
    fun setUpMockBukkit() {
        server = MockBukkit.mock()
        multiverseCore = MockBukkit.load(MultiverseCore::class.java)
        multiverseNetherPortals = MockBukkit.load(MultiverseNetherPortals::class.java)
    }

    @AfterTest
    fun tearDownMockBukkit() {
        server.pluginManager.disablePlugin(multiverseNetherPortals)
        server.pluginManager.disablePlugin(multiverseCore)
        MockBukkit.unmock()
    }
}
