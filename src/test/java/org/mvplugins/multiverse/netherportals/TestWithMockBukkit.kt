package org.mvplugins.multiverse.netherportals

import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mvplugins.multiverse.core.MultiverseCore
import org.mvplugins.multiverse.core.inject.PluginServiceLocator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base test fixture that loads MockBukkit, Multiverse-Core, and Multiverse-NetherPortals.
 */
abstract class TestWithMockBukkit {

    protected lateinit var server: ServerMock
    protected lateinit var multiverseCore: MultiverseCore
    protected lateinit var multiverseNetherPortals: MultiverseNetherPortals
    protected lateinit var serviceLocator: PluginServiceLocator

    @BeforeTest
    fun setUpMockBukkit() {
        server = MockBukkit.mock()
        multiverseCore = MockBukkit.load(MultiverseCore::class.java)
        multiverseNetherPortals = MockBukkit.load(MultiverseNetherPortals::class.java)
        serviceLocator = multiverseNetherPortals.serviceLocator
    }

    @AfterTest
    fun tearDownMockBukkit() {
        server.pluginManager.disablePlugin(multiverseNetherPortals)
        server.pluginManager.disablePlugin(multiverseCore)
        MockBukkit.unmock()
    }

    protected fun getResourceAsText(path: String): String? =
        object {}.javaClass.getResource(path)?.readText()

    protected fun writeResourceFileToPluginDataFolder(resourcePath: String, dataPath: String) {
        val resourceText = requireNotNull(getResourceAsText(resourcePath))
        multiverseNetherPortals.dataFolder.toPath().resolve(dataPath).toFile().writeText(resourceText)
    }
}
