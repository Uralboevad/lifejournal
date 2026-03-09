package com.example.lifejournal.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val dao: JournalDao) {

    val allEntries: Flow<List<JournalEntry>> = dao.getAllEntries()

    suspend fun getEntryById(id: Int): JournalEntry? = dao.getEntryById(id)

    suspend fun insertEntry(entry: JournalEntry): Long = dao.insertEntry(entry)

    suspend fun updateEntry(entry: JournalEntry) = dao.updateEntry(entry)

    suspend fun deleteEntry(entry: JournalEntry) = dao.deleteEntry(entry)

    fun searchEntries(query: String): Flow<List<JournalEntry>> = dao.searchEntries(query)
}