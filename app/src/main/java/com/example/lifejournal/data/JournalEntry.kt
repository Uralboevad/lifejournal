package com.example.lifejournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val text: String,
    val photoPath: String? = null,
    val audioPath: String? = null,
    val weatherDescription: String? = null,
    val weatherIcon: String? = null,
    val temperature: Double? = null,
    val mood: String? = null,
    val date: String = ""
)