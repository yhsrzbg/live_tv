package com.yhsrzbg.live_tv.core

import com.yhsrzbg.live_tv.core.model.LiveColor
import com.yhsrzbg.live_tv.core.model.LiveMessage
import com.yhsrzbg.live_tv.core.model.LiveMessageType
import com.yhsrzbg.live_tv.core.model.LiveSuperChatMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelCompatibilityTest {

    @Test
    fun `live message keeps username and gift type`() {
        val message = LiveMessage(
            type = LiveMessageType.Gift,
            userName = "bob",
            message = "gift",
            color = LiveColor(255, 255, 255),
            data = 0,
        )
        assertEquals("bob", message.userName)
    }

    @Test
    fun `super chat message model exists`() {
        val sc = LiveSuperChatMessage(
            userName = "alice",
            face = "avatar",
            message = "nice",
            price = 30,
            startTime = 1_700_000_000_000,
            endTime = 1_700_000_060_000,
            backgroundColor = "#ff0000",
            backgroundBottomColor = "#000000",
        )
        assertEquals("alice", sc.userName)
        assertEquals(30, sc.price)
    }
}

