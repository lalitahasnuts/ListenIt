package com.example.listenit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.listenit.service.RadioItem

@Composable
fun QueueAndFavoriteLists(
    queueItems: List<RadioItem>,
    favoriteItems: List<RadioItem>,
    currentPlayingItem: RadioItem?,
    onQueueItemClick: (RadioItem) -> Unit,
    onFavoriteItemClick: (RadioItem) -> Unit,
    onToggleFavorite: (RadioItem) -> Unit,
    onAddCustomStation: (RadioItem) -> Unit, // Новый параметр для добавления станции
    modifier: Modifier = Modifier
) {
    // Цвета
    val colorGreenAsideBg = Color(0x4D85A37F)
    val colorGreenAsideBgHover = Color(0x8085A37F)
    val colorGreenItemPlaying = Color(0x4D6E8A68)

    // Состояния для формы добавления
    var showAddForm by remember { mutableStateOf(false) }
    var stationName by remember { mutableStateOf("") }
    var stationUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Список очереди
        Column(
            modifier = Modifier
                .width(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorGreenAsideBg)
                .border(1.dp, colorGreenAsideBg, RoundedCornerShape(16.dp))
                .padding(15.dp)
        ) {
            Text(
                text = "Очередь",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .height(200.dp) // Фиксированная высота для скролла
                    .fillMaxWidth()
            ) {
                if (queueItems.isEmpty()) {
                    Text(
                        text = "Очередь пуста",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(vertical = 15.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(queueItems) { item ->
                            QueueListItem(
                                item = item,
                                isPlaying = item == currentPlayingItem,
                                isFavorite = favoriteItems.contains(item),
                                onClick = { onQueueItemClick(item) },
                                onToggleFavorite = { onToggleFavorite(item) },
                                backgroundColor = if (item == currentPlayingItem) {
                                    colorGreenItemPlaying
                                } else {
                                    Color.Transparent
                                },
                                hoverColor = colorGreenAsideBgHover
                            )
                        }
                    }
                }
            }

            // Кнопка добавления новой станции
            Button(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorGreenAsideBgHover
                )
            ) {
                Text("Добавить станцию", color = Color.White)
            }

            // Форма добавления новой станции
            if (showAddForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stationName,
                        onValueChange = { stationName = it },
                        label = { Text("Название станции") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stationUrl,
                        onValueChange = { stationUrl = it },
                        label = { Text("URL потока") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showAddForm = false
                                stationName = ""
                                stationUrl = ""
                            }
                        ) {
                            Text("Отмена")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (stationName.isNotBlank() && stationUrl.isNotBlank()) {
                                    val newStation = RadioItem(
                                        id = "custom_${System.currentTimeMillis()}",
                                        name = stationName,
                                        source = stationUrl
                                    )
                                    onAddCustomStation(newStation)
                                    showAddForm = false
                                    stationName = ""
                                    stationUrl = ""
                                }
                            },
                            enabled = stationName.isNotBlank() && stationUrl.isNotBlank()
                        ) {
                            Text("Добавить")
                        }
                    }
                }
            }
        }
            // Список избранного
            Column(
                modifier = Modifier
                    .width(350.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorGreenAsideBg)
                    .border(1.dp, colorGreenAsideBg, RoundedCornerShape(16.dp))
                    .padding(15.dp)
            ) {
                Text(
                    text = "Избранное",
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (favoriteItems.isEmpty()) {
                    Text(
                        text = "Нет избранных станций",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(vertical = 15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favoriteItems) { item ->
                            FavoriteListItem(
                                item = item,
                                isPlaying = item == currentPlayingItem,
                                onClick = { onFavoriteItemClick(item) },
                                backgroundColor = if (item == currentPlayingItem) {
                                    colorGreenItemPlaying
                                } else {
                                    Color.Transparent
                                },
                                hoverColor = colorGreenAsideBgHover
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun QueueListItem(
    item: RadioItem,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    backgroundColor: Color,
    hoverColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHovered && !isPlaying) hoverColor else backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.name,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(15.dp))

        Icon(
            painter = painterResource(if (isFavorite) R.drawable.icon_like else R.drawable.icon_liked),
            contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
            modifier = Modifier
                .size(25.dp)
                .clickable { onToggleFavorite() },
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun FavoriteListItem(
    item: RadioItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    hoverColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHovered && !isPlaying) hoverColor else backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.name,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}