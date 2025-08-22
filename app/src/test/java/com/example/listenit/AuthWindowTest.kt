package com.example.listenit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthWindowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Моки сервисов можно инициализировать здесь при необходимости
    }

    @Test
    fun authScreen_shouldDisplayCorrectUI() {
        composeTestRule.setContent {
            Auth(
                onNavigateToReg = {},
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Вход").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Пароль").assertExists()
        composeTestRule.onNodeWithText("Войти").assertExists()
        composeTestRule.onNodeWithText("Ещё нет аккаунта? Зарегистрироваться").assertExists()
    }

    @Test
    fun authScreen_shouldShowErrorWhenFieldsEmpty() {
        composeTestRule.setContent {
            Auth(
                onNavigateToReg = {},
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Войти").performClick()
        composeTestRule.onNodeWithText("Заполните все поля").assertExists()
    }

    @Test
    fun authScreen_shouldNavigateToRegScreen() {
        var navigatedToReg = false

        composeTestRule.setContent {
            Auth(
                onNavigateToReg = { navigatedToReg = true },
                onNavigateToMain = {}
            )
        }

        composeTestRule.onNodeWithText("Ещё нет аккаунта? Зарегистрироваться").performClick()
        assert(navigatedToReg)
    }
}