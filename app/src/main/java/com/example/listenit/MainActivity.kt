package com.example.listenit

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.listenit.service.RadioItem
import com.example.listenit.service.RadioService
import com.example.listenit.ui.theme.ListenItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListenItTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authToken) {
        if (authToken != null) {
            RadioService.setAuthToken(authToken)
        }
    }

    when (currentScreen) {
        AppScreen.AUTH -> Auth(
            onNavigateToReg = { currentScreen = AppScreen.REG },
            onNavigateToMain = { token ->
                authToken = token as? String
                currentScreen = AppScreen.MAIN
            }
        )
        AppScreen.REG -> Reg(
            onNavigateToAuth = { currentScreen = AppScreen.AUTH },
            onNavigateToMain = { token ->
                authToken = token as? String
                currentScreen = AppScreen.MAIN
            }
        )
        AppScreen.MAIN -> {
            authToken?.let { token ->
                ListenItMain(
                    authToken = token,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Если токен null, возвращаем на экран авторизации
                currentScreen = AppScreen.AUTH
            }
        }
    }
}

@Composable
fun ListenItMain(authToken: String?, modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var radioStations by remember { mutableStateOf<List<RadioItem>>(emptyList()) }
    var currentStationIndex by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var queueItems by remember { mutableStateOf<List<RadioItem>>(emptyList()) }
    var favoriteItems by remember { mutableStateOf<List<RadioItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Добавляем обработчик
    val onAddCustomStation = { station: RadioItem ->
        queueItems = queueItems + station
    }

    val currentStation = remember(currentStationIndex, radioStations) {
        if (radioStations.isNotEmpty() && currentStationIndex in radioStations.indices) {
            radioStations[currentStationIndex]
        } else {
            null
        }
    }

    // Загрузка радиостанций при первом запуске
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null

            // Загружаем основные станции
            val stationsResult = RadioService.getRadio()
            if (stationsResult.stations.isNotEmpty()) {
                radioStations = stationsResult.stations
                queueItems = stationsResult.stations
                currentStationIndex = 0 // Сбрасываем выбор на первую станцию
            } else {
                errorMessage = "Список станций пуст"
            }

        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.localizedMessage}"
            println("Ошибка при загрузке: ${e.stackTraceToString()}")
        } finally {
            isLoading = false
        }
    }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    // Очистка MediaPlayer при уничтожении Composable
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // Обработка воспроизведения/паузы
    LaunchedEffect(isPlaying, currentStation) {
        if (isPlaying && currentStation != null) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(currentStation.source)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    it.start()
                }
                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    errorMessage = "Ошибка воспроизведения (код $what, $extra)"
                    isPlaying = false
                    true
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка при воспроизведении: ${e.message}"
                isPlaying = false
            }
        } else {
            mediaPlayer.reset()
        }
    }

    fun nextStation() {
        if (radioStations.isNotEmpty()) {
            currentStationIndex = (currentStationIndex + 1) % radioStations.size
            if (!isPlaying) isPlaying = true
        }
    }

    fun prevStation() {
        if (radioStations.isNotEmpty()) {
            currentStationIndex = (currentStationIndex - 1 + radioStations.size) % radioStations.size
            if (!isPlaying) isPlaying = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircleBackground(theme = if (isPlaying) "green" else "yellow")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Текст
            Text(
                text = "listen!",
                color = Color.Black,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, top = 26.dp)
            )

            // Кассета
            Box(
                modifier = Modifier
                    .width(540.dp)
                    .height(260.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.audio),
                    contentDescription = "Кассета",
                    modifier = Modifier.matchParentSize()
                )

                // Отображение текущей радиостанции
                currentStation?.let { station ->
                        Text(
                            text = station.name,
                            color = Color.Black,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 20.dp)
                        )
                }

                // Отображение ошибки
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp)
                    )
                }

                // Стрелка (слева) - нужно зеркальное отображение
                Image(
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = "Следующая станция",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(50.dp)
                        .rotate(180f)
                        .clickable(
                            enabled = radioStations.isNotEmpty(),
                            onClick = { prevStation() }
                        )
                )

                // Стрелка (справа)
                Image(
                    painter = painterResource(R.drawable.arrow),
                    contentDescription = "Предыдущая станция",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(50.dp)
                        .clickable(
                            enabled = radioStations.isNotEmpty(),
                            onClick = { nextStation() }
                        )
                )

                // Кнопка воспроизведения/паузы
                Button(
                    onClick = {
                        if (currentStation != null) {
                            isPlaying = !isPlaying
                        } else {
                            errorMessage = "Нет доступных станций"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color.Green else Color.Red,
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = currentStation != null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(70.dp)
                        .padding(bottom = 16.dp),
                    shape = CircleShape
                ) {
                    if (isPlaying) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(0f, size.height)
                                lineTo(size.width / 2, size.height / 2)
                                close()
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(0f, size.height / 4)
                                lineTo(0f, size.height * 3 / 4)
                                moveTo(size.width / 2, size.height / 4)
                                lineTo(size.width / 2, size.height * 3 / 4)
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }

                // Кнопка шазама
                Button(
                    onClick = { /* Логика для шазама */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .size(55.dp),
                    shape = CircleShape
                ) {
                    Text("S", color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center)
                }
            }
            // Добавляем компонент QueueAndFavoriteLists
            QueueAndFavoriteLists(
                queueItems = queueItems,
                favoriteItems = favoriteItems,
                currentPlayingItem = currentStation,
                onQueueItemClick = { station ->
                    currentStationIndex = radioStations.indexOf(station)
                    isPlaying = true
                },
                onFavoriteItemClick = { station ->
                    currentStationIndex = radioStations.indexOf(station)
                    isPlaying = true
                },
                onToggleFavorite = { station ->
                    favoriteItems = if (favoriteItems.contains(station)) {
                        favoriteItems - station
                    } else {
                        favoriteItems + station
                    }
                },
                onAddCustomStation = onAddCustomStation // Передаем обработчик
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListenItAppPreview() {
    ListenItTheme {
        // Создаем mock-версию для предпросмотра
        ListenItMainPreview()
    }
}

@Composable
fun ListenItMainPreview() {
    var isPlaying by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(50) }

    // Mock-данные для предпросмотра
    val mockRadioStations = listOf(
        RadioItem("1", "Radio Paradise", "http://stream.example.com/paradise"),
        RadioItem("2", "Classic Rock", "http://stream.example.com/classic"),
        RadioItem("3", "Jazz FM", "http://stream.example.com/jazz"),
        RadioItem("4", "Chill Out", "http://stream.example.com/chill")
    )
    // Mock-данные для очереди и избранного
    var queueItems by remember { mutableStateOf(mockRadioStations.take(2)) }
    var favoriteItems by remember { mutableStateOf(mockRadioStations.takeLast(2)) }
    var currentStation by remember { mutableStateOf(mockRadioStations.first()) }

    val onAddCustomStation = { station: RadioItem ->
        queueItems = queueItems + station
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircleBackground(theme = if (isPlaying) "green" else "yellow")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "listen!",
                color = Color.Black,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, top = 26.dp)
            )

            Box(
                modifier = Modifier
                    .width(540.dp)
                    .height(260.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.audio),
                    contentDescription = "Кассета",
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    text = mockRadioStations.first().name,
                    color = Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp)
                )

                // Стрелка (слева) - нужно зеркальное отображение
                Image(
                    painter = painterResource(R.drawable.arrow), // Та же стрелка, но повернутая
                    contentDescription = "Следующая станция",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(50.dp)
                        .rotate(180f) // Поворачиваем стрелку на 180 градусов
                        .clickable(
                            //enabled = radioStations.isNotEmpty(),
                            onClick = { /**/ }
                        )
                )

                // Стрелка (справа)
                Image(
                    painter = painterResource(R.drawable.arrow), // Ваша стрелка (должна указывать влево)
                    contentDescription = "Предыдущая станция",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(50.dp)
                        .clickable(
                            //enabled = radioStations.isNotEmpty(),
                            onClick = { /**/ }
                        )
                )

                Button(
                    onClick = { isPlaying = !isPlaying },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color.Green else Color.Red
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(70.dp)
                        .padding(start = 16.dp, bottom = 16.dp),
                    shape = CircleShape
                ) {
                    if (isPlaying) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(0f, size.height)
                                lineTo(size.width / 2, size.height / 2)
                                close()
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(0f, size.height / 4)
                                lineTo(0f, size.height * 3 / 4)
                                moveTo(size.width / 2, size.height / 4)
                                lineTo(size.width / 2, size.height * 3 / 4)
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }

                Button(
                    onClick = { /* Шазам логика */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                        .size(55.dp),
                    shape = CircleShape
                ) {
                    Text("S", color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center)
                }
            }
            // Добавляем списки очереди и избранного
            QueueAndFavoriteLists(
                queueItems = queueItems,
                favoriteItems = favoriteItems,
                currentPlayingItem = if (isPlaying) currentStation else null,
                onQueueItemClick = { station ->
                    currentStation = station
                    isPlaying = true
                },
                onFavoriteItemClick = { station ->
                    currentStation = station
                    isPlaying = true
                },
                onToggleFavorite = { station ->
                    favoriteItems = if (favoriteItems.contains(station)) {
                        favoriteItems - station
                    } else {
                        favoriteItems + station
                    }
                },
                onAddCustomStation = onAddCustomStation
            )
        }
    }
}