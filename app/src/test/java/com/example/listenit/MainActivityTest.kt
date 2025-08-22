package com.example.listenit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.listenit.service.RadioItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.Modifier

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val testStations = listOf(
        RadioItem("1", "Radio Paradise", "http://stream.example.com/paradise"),
        RadioItem("2", "Classic Rock", "http://stream.example.com/classic")
    )

    @Before
    fun setup() {
        // Здесь можно настроить моки или тестовые данные
    }

    @Test
    fun mainScreen_shouldDisplayCorrectUI() {
        composeTestRule.setContent {
            ListenItMain(
                authToken = "test-token",
                modifier = Modifier.fillMaxSize()
            )
        }

        composeTestRule.onNodeWithText("listen!").assertExists()
        composeTestRule.onNodeWithContentDescription("Кассета").assertExists()
        composeTestRule.onNodeWithContentDescription("Следующая станция").assertExists()
        composeTestRule.onNodeWithContentDescription("Предыдущая станция").assertExists()
    }

    @Test
    fun mainScreen_shouldSwitchStations() {
        composeTestRule.setContent {
            ListenItMainPreview() // Используем preview версию с тестовыми данными
        }

        composeTestRule.onNodeWithContentDescription("Следующая станция").performClick()
        composeTestRule.onNodeWithText("Classic Rock").assertExists()
    }

    @Test
    fun mainScreen_shouldPlayAndPause() {
        composeTestRule.setContent {
            ListenItMainPreview()
        }

        // Первый клик - воспроизведение
        composeTestRule.onNodeWithContentDescription("Кнопка воспроизведения/паузы").performClick()

        // Второй клик - пауза
        composeTestRule.onNodeWithContentDescription("Кнопка воспроизведения/паузы").performClick()
    }
}