package org.mvplugins.multiverse.netherportals.utils.customportals

import org.bukkit.Location
import org.mvplugins.multiverse.netherportals.TestWithMockBukkit
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomPortalsHandlerTest : TestWithMockBukkit() {

    @Test
    fun `A handle check can unregister itself while checks are running`() {
        val handler = serviceLocator.getService(CustomPortalsHandler::class.java)
        val location = Location(server.addSimpleWorld("custom_portal_world"), 0.0, 64.0, 0.0)
        lateinit var selfRemovingCheck: CustomPortalsHandleCheck
        selfRemovingCheck = CustomPortalsHandleCheck { _, _ ->
            handler.unregisterHandleCheck(selfRemovingCheck)
            false
        }
        val remainingCheck = CustomPortalsHandleCheck { _, _ -> true }
        handler.registerHandleCheck(selfRemovingCheck)
        handler.registerHandleCheck(remainingCheck)

        assertTrue(handler.isHandledByCustomPortals(null, location))

        handler.unregisterHandleCheck(remainingCheck)
        assertFalse(handler.isHandledByCustomPortals(null, location))
    }

    @Test
    fun `Unregistered handle check is no longer called`() {
        val handler = serviceLocator.getService(CustomPortalsHandler::class.java)
        val location = Location(server.addSimpleWorld("unregistered_portal_world"), 0.0, 64.0, 0.0)
        val check = CustomPortalsHandleCheck { _, _ -> true }
        handler.registerHandleCheck(check)
        assertTrue(handler.isHandledByCustomPortals(null, location))

        handler.unregisterHandleCheck(check)

        assertFalse(handler.isHandledByCustomPortals(null, location))
    }
}
