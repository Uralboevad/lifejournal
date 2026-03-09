package com.example.lifejournal.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lifejournal.data.JournalEntry
import com.example.lifejournal.data.JournalViewModel
import com.example.lifejournal.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onCreateEntry: () -> Unit,
    onViewEntry: (Int) -> Unit
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val query   by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Life Journal", style = MaterialTheme.typography.headlineMedium)
                        Text("${entries.size} memories", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEntry,
                containerColor = Coral,
                contentColor   = CardWhite,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "New entry") }
        },
        containerColor = CreamWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search bar
            OutlinedTextField(
                value         = query,
                onValueChange = viewModel::setSearchQuery,
                placeholder   = { Text("Search entries…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                shape    = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Coral,
                    unfocusedBorderColor    = PeachPastel,
                    focusedContainerColor   = CardWhite,
                    unfocusedContainerColor = CardWhite
                ),
                singleLine = true
            )

            if (entries.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        JournalCard(
                            entry    = entry,
                            onClick  = { onViewEntry(entry.id) },
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete entry?") },
            text             = { Text("This cannot be undone.") },
            confirmButton    = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = Coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or mood placeholder
            if (entry.photoPath != null) {
                AsyncImage(
                    model              = entry.photoPath,
                    contentDescription = "Photo",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LavenderLight),
                    contentAlignment = Alignment.Center
                ) { Text(entry.mood ?: "📔", style = MaterialTheme.typography.headlineMedium) }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.title.ifBlank { "Untitled" },
                        style    = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Show weather emoji stored directly as icon string
                    entry.weatherIcon?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                }
                Text(
                    entry.text,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp)
                ) {
                    Text(formatDate(entry.date), style = MaterialTheme.typography.labelSmall)
                    entry.temperature?.let {
                        Text("${it.toInt()}°C", style = MaterialTheme.typography.labelSmall, color = Peach)
                    }
                    if (entry.audioPath != null) Text("🎤", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarmGrey)
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📔", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text("No entries yet", style = MaterialTheme.typography.headlineMedium)
            Text("Tap + to write your first memory", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun formatDate(dateStr: String): String = try {
    val dt = LocalDateTime.parse(dateStr)
    dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm"))
} catch (e: Exception) { dateStr }