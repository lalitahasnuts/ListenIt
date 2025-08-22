package com.example.listenit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.random.Random

// Цвета
val yellowBgColor = Color(0xFFF9F4D2)
val greenBgColor = Color(0xFFB8E0B1)

val circleColors = mapOf(
    "yellow" to listOf(
        Color(0xFFE0DBBD), Color(0xFFD2CEB1), Color(0xFFEBE7C9),
        Color(0xFFE7E1BA), Color(0xFFF0EBC0), Color(0xFFD8D3A5),
        Color(0xFFEEE8B8), Color(0xFFE5DFB3), Color(0xFFF5F0C5),
        Color(0xFFDAD4A8), Color(0xFFE3DDB0), Color(0xFFEDE7BE)
    ),
    "green" to listOf(
        Color(0xFFA1CC99), Color(0xFFA1C39A), Color(0xFFAED4A6),
        Color(0xFFACCCA5), Color(0xFF9FC192), Color(0xFFB5D9AD),
        Color(0xFFA8CE9F), Color(0xFFB1D7A8), Color(0xFF9DC890),
        Color(0xFFABD2A2), Color(0xFFB7DCAD), Color(0xFFA3CA9B)
    )
)

// Размеры кругов в vw (будем конвертировать в dp)
val circleSizes = listOf(
    90f, 100f, 500f, 150f, 90f, 550f,
    170f, 300f, 200f, 500f, 160f, 80f
)

@Composable
fun CircleBackground(theme: String = "yellow") {
    val backgroundColor = if (theme == "yellow") yellowBgColor else greenBgColor
    val colors = circleColors[theme] ?: circleColors["yellow"]!!

    // Количество кругов и их параметры
    val circleCount = 12
    val scales = remember { List(circleCount) { Animatable(0.1f) } }

    // Запускаем анимации
    LaunchedEffect(Unit) {
        scales.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 0.5f + index * 0.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 10000 + index * 1000,
                            delayMillis = index * 150,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val screenWidthDp = constraints.maxWidth.toFloat().dp
        val screenHeightDp = constraints.maxHeight.toFloat().dp

        // Вычисление позиций равномерно распределенных кругов по экрану
        val positions = remember {
            List(circleCount) { index ->
                // Распределение по сетке 4x3 с небольшой случайностью
                val row = index / 4
                val col = index % 4

                val xPos = (col * 0.25f + 0.15f + Random.nextFloat() * 0.1f - 0.05f) * screenWidthDp.value
                val yPos = (row * 0.35f + 0.15f + Random.nextFloat() * 0.1f - 0.05f) * screenHeightDp.value

                Offset(x = xPos, y = yPos)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(circleCount) { index ->
                val size = (circleSizes[index % circleSizes.size]) * scales[index].value
                drawCircle(
                    color = colors[index % colors.size],
                    radius = size / 2,
                    center = positions[index],
                    alpha = 0.7f - index * 0.02f
                )
            }
        }
    }
}
