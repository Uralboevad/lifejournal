package com.example.lifejournal.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.lifejournal.WeatherData
import com.example.lifejournal.audio.AudioRecorder
import com.example.lifejournal.data.JournalEntry
import com.example.lifejournal.data.JournalViewModel
import com.example.lifejournal.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

val MOODS = listOf("😊", "😔", "😌", "🥰", "😤", "😴", "🤔", "🎉")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEntryScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("😊") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isFetchingWeather by remember { mutableStateOf(false) }
    var weatherFetched by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf(false) }

    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasMicPermission by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) photoPath = getPathFromUri(context, photoUri) }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            photoUri = createImageUri(context)
            photoUri?.let { cameraLauncher.launch(it) }
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            audioPath = audioRecorder.startRecording()
            isRecording = true
        }
    }

    DisposableEffect(Unit) { onDispose { audioRecorder.release() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Entry", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val entry = JournalEntry(
                                title              = title.ifBlank { "Untitled" },
                                text               = text,
                                mood               = selectedMood,
                                photoPath          = photoPath,
                                audioPath          = audioPath,
                                weatherDescription = weatherData?.description,
                                weatherIcon        = weatherData?.icon,
                                temperature        = weatherData?.temperature,
                                date               = LocalDateTime.now().toString()
                            )
                            viewModel.insertEntry(entry) { onNavigateBack() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = CardWhite, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamWhite)
            )
        },
        containerColor = CreamWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Title
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Title") },
                placeholder   = { Text("What's today about?") },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = journalTextFieldColors(),
                singleLine    = true
            )

            // Mood
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Mood", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MOODS.forEach { mood ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (mood == selectedMood) CoralLight else CreamSurface)
                                    .clickable { selectedMood = mood },
                                contentAlignment = Alignment.Center
                            ) { Text(mood, fontSize = 20.sp) }
                        }
                    }
                }
            }

            // Photo
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Photo", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    if (photoUri != null) {
                        AsyncImage(
                            model            = photoUri,
                            contentDescription = "Taken photo",
                            contentScale     = ContentScale.Crop,
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick  = { cameraPermLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral)                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (photoUri != null) "Retake Photo" else "Take Photo")
                    }
                    if (!hasCameraPermission) {
                        Text("Camera permission needed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGrey, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Voice Note
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Voice Note", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    if (audioPath != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintLight)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🎤 Recording saved", style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = {
                                if (isPlaying) {
                                    audioRecorder.stopPlayback(); isPlaying = false
                                } else {
                                    audioRecorder.startPlayback(audioPath!!) { isPlaying = false }
                                    isPlaying = true
                                }
                            }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Stop", tint = Mint
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (isRecording) {
                                audioPath = audioRecorder.stopRecording()
                                isRecording = false
                            } else {
                                micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = if (isRecording) CoralDark else Lavender),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isRecording) "Stop Recording" else "Record Voice Note")
                    }
                    if (!hasMicPermission && audioPath == null) {
                        Text("Microphone permission needed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGrey, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Weather
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Weather", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    if (weatherData != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(weatherData!!.icon, fontSize = 32.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${weatherData!!.temperature.toInt()}°C in Oulu",
                                    style = MaterialTheme.typography.titleLarge)
                                Text(weatherData!!.description,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (weatherError) {
                        Text("⚠️ Could not fetch weather. Check your connection.",
                            style = MaterialTheme.typography.bodyMedium, color = CoralDark,
                            modifier = Modifier.padding(bottom = 8.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            isFetchingWeather = true; weatherError = false
                            scope.launch {
                                val result = WeatherApi.getCurrentWeather()
                                weatherData    = result
                                weatherError   = result == null
                                isFetchingWeather = false
                                weatherFetched = true
                            }
                        },
                        enabled  = !isFetchingWeather,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Peach)                    ) {
                        if (isFetchingWeather) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = Peach, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.WbSunny, null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (weatherFetched) "Refresh Weather" else "Fetch Weather")
                    }
                }
            }

            // Journal text
            OutlinedTextField(
                value         = text,
                onValueChange = { text = it },
                label         = { Text("Journal Entry") },
                placeholder   = { Text("What's on your mind today?") },
                modifier      = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 160.dp),
                shape         = RoundedCornerShape(12.dp),
                colors        = journalTextFieldColors(),
                maxLines      = 20
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun createImageUri(context: Context): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "journal_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}

fun getPathFromUri(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    val cursor = context.contentResolver.query(
        uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null
    )
    return cursor?.use {
        if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)) else null
    }
}

@Composable
fun journalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Coral,
    unfocusedBorderColor    = PeachPastel,
    focusedContainerColor   = CardWhite,
    unfocusedContainerColor = CardWhite,
    focusedLabelColor       = Coral
)