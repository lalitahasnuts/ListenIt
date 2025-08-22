package com.example.listenit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.listenit.service.RadioItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueueListTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val testQueueItems = listOf(
        RadioItem("1", "Station 1", "http://example.com/1"),
        RadioItem("2", "Station 2", "http://example.com/2")
    )

    private val testFavoriteItems = listOf(
        RadioItem("3", "Favorite 1", "http://example.com/fav1")
    )

    @Test
    fun queueList_shouldDisplayItems() {
        composeTestRule.setContent {
            QueueAndFavoriteLists(
                queueItems = testQueueItems,
                favoriteItems = testFavoriteItems,
                currentPlayingItem = null,
                onQueueItemClick = {},
                onFavoriteItemClick = {},
                onToggleFavorite = {},
                onAddCustomStation = {}
            )
        }

        composeTestRule.onNodeWithText("Station 1").assertExists()
        composeTestRule.onNodeWithText("Favorite 1").assertExists()
    }

    @Test
    fun queueList_shouldShowAddForm() {
        composeTestRule.setContent {
            QueueAndFavoriteLists(
                queueItems = testQueueItems,
                favoriteItems = testFavoriteItems,
                currentPlayingItem = null,
                onQueueItemClick = {},
                onFavoriteItemClick = {},
                onToggleFavorite = {},
                onAddCustomStation = {}
            )
        }

        composeTestRule.onNodeWithText("Добавить станцию").performClick()
        composeTestRule.onNodeWithText("Название станции").assertExists()
    }

    @Test
    fun queueList_shouldAddCustomStation() {
        var addedStation: RadioItem? = null

        composeTestRule.setContent {
            QueueAndFavoriteLists(
                queueItems = testQueueItems,
                favoriteItems = testFavoriteItems,
                currentPlayingItem = null,
                onQueueItemClick = {},
                onFavoriteItemClick = {},
                onToggleFavorite = {},
                onAddCustomStation = { addedStation = it }
            )
        }

        composeTestRule.onNodeWithText("Добавить станцию").performClick()
        composeTestRule.onNodeWithText("Название станции").performTextInput("New Station")
        composeTestRule.onNodeWithText("URL потока").performTextInput("http://new.station")
        composeTestRule.onNodeWithText("Добавить").performClick()

        assert(addedStation?.name == "New Station")
    }
}