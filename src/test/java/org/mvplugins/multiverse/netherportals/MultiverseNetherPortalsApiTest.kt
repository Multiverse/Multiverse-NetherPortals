package org.mvplugins.multiverse.netherportals

import org.mvplugins.multiverse.netherportals.config.NetherPortalsConfig
import org.mvplugins.multiverse.netherportals.links.LinksManager
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MultiverseNetherPortalsApiTest : TestWithMockBukkit() {

    @Test
    fun `API exposes config and links manager`() {
        val api = MultiverseNetherPortalsApi.get()
        val config = api.netherPortalsConfig
        val linksManager = api.linksManager

        assertTrue(MultiverseNetherPortalsApi.isLoaded())
        assertSame(config, serviceLocator.getService(NetherPortalsConfig::class.java))
        assertSame(linksManager, serviceLocator.getService(LinksManager::class.java))
        assertSame(serviceLocator, api.serviceLocator)
    }

    @Test
    fun `API is registered as a Bukkit service`() {
        val registration = server.servicesManager.getRegistration(MultiverseNetherPortalsApi::class.java)

        assertSame(MultiverseNetherPortalsApi.get(), registration?.provider)
    }

    @Test
    fun `whenLoaded callback runs immediately when API is loaded`() {
        var callbackApi: MultiverseNetherPortalsApi? = null

        MultiverseNetherPortalsApi.whenLoaded { callbackApi = it }

        assertSame(MultiverseNetherPortalsApi.get(), callbackApi)
    }
}
