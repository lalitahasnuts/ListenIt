package com.example.listenit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegWindowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun regScreen_shouldDisplayCorrectUI() {
        composeTestRule.setContent {
            Reg(
                onNavigateToAuth = {},
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Регистрация").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Пароль").assertExists()
        composeTestRule.onNodeWithText("Зарегистрироваться").assertExists()
        composeTestRule.onNodeWithText("Уже есть аккаунт? Войти").assertExists()
    }

    @Test
    fun regScreen_shouldShowErrorWhenFieldsEmpty() {
        composeTestRule.setContent {
            Reg(
                onNavigateToAuth = {},
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()
        composeTestRule.onNodeWithText("Заполните все поля").assertExists()
    }

    @Test
    fun regScreen_shouldNavigateToAuthScreen() {
        var navigatedToAuth = false

        composeTestRule.setContent {
            Reg(
                onNavigateToAuth = { navigatedToAuth = true },
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Уже есть аккаунт? Войти").performClick()
        assert(navigatedToAuth)
    }
}