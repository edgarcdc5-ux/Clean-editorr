package com.cleaneditor.app

import com.cleaneditor.app.navigation.NavigationDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationUnitTest {

    @Test
    fun verifyNavigationDestinations() {
        val items = NavigationDestination.items
        assertEquals(5, items.size)
        assertEquals(NavigationDestination.Home, items[0])
        assertEquals(NavigationDestination.Files, items[1])
        assertEquals(NavigationDestination.Reminders, items[2])
        assertEquals(NavigationDestination.Ai, items[3])
        assertEquals(NavigationDestination.Settings, items[4])
    }

    @Test
    fun verifyTestTags() {
        NavigationDestination.items.forEach { destination ->
            assertNotNull(destination.testTag)
            assert(destination.testTag.startsWith("nav_item_"))
        }
    }
}
