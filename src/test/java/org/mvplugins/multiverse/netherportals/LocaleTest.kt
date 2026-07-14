package org.mvplugins.multiverse.netherportals

import org.mvplugins.multiverse.core.command.MVCommandManager
import org.mvplugins.multiverse.core.locale.message.Message
import org.mvplugins.multiverse.netherportals.locale.MVNPi18n
import java.util.Properties
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocaleTest : TestWithMockBukkit() {

    private lateinit var commandManager: MVCommandManager

    @BeforeTest
    fun setUp() {
        commandManager = assertNotNull(serviceLocator.getService(MVCommandManager::class.java))
    }

    @Test
    fun `Default locale bundle contains every message key`() {
        val properties = Properties()
        assertNotNull(javaClass.getResourceAsStream("/multiverse-netherportals_en.properties")).use {
            properties.load(it)
        }

        MVNPi18n.entries.forEach { message ->
            assertTrue(properties.containsKey(message.messageKey.key), "Missing locale key: ${message.messageKey.key}")
        }
    }

    @Test
    fun `Locale bundle is registered`() {
        assertEquals(
            "Displays all configured portal links.",
            Message.of(MVNPi18n.LIST_DESCRIPTION).formatted(commandManager.consoleCommandIssuer)
        )
    }
}
