package com.example.lifejournal.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifejournal.WeatherApi
import com.example.lifejournal.audio.AudioRecorder
import com.example.lifejournal.data.JournalEntry
import com.example.lifejournal.data.JournalViewModel
import com.example.lifejournal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewEntryScreen(
    entryId: Int,
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var entry by remember { mutableStateOf<JournalEntry?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorder(context) }

    LaunchedEffect(entryId) { entry = viewModel.getEntryById(entryId) }
    DisposableEffect(Unit) { onDispose { audioRecorder.release() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.title ?: "Entry", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamWhite)
            )
        },
        containerColor = CreamWhite
    ) { padding ->
        val e = entry ?: return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header: mood + date + weather badge
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(e.mood ?: "📔", fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(e.title, style = MaterialTheme.typography.titleLarge)
                            Text(formatDate(e.date), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    // Weather badge
                    if (e.weatherIcon != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(WeatherApi.getWeatherIcon(
                                e.weatherIcon.toIntOrNull() ?: 0
                            ), fontSize = 28.sp)
                            e.temperature?.let {
                                Text("${it.toInt()}°C",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Peach, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Photo
            if (e.photoPath != null) {
                AsyncImage(
                    model              = e.photoPath,
                    contentDescription = "Journal photo",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Journal text
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✍️ Entry", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        e.text.ifBlank { "(no text)" },
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            // Audio playback
            if (e.audioPath != null) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MintLight)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎤", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Voice Note", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    if (isPlaying) "Playing…" else "Tap to play",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    audioRecorder.stopPlayback(); isPlaying = false
                                } else {
                                    audioRecorder.startPlayback(e.audioPath) { isPlaying = false }
                                    isPlaying = true
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Mint)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = CardWhite
                            )
                        }
                    }
                }
            }

            // Weather detail
            if (e.weatherDescription != null || e.temperature != null) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // weatherIcon stored as the emoji string directly
                        Text(e.weatherIcon ?: "🌡️", fontSize = 40.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Weather that day", style = MaterialTheme.typography.titleLarge)
                            e.weatherDescription?.let {
                                Text(it.replaceFirstChar { c -> c.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            e.temperature?.let {
                                Text("${it.toInt()}°C",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Peach, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}