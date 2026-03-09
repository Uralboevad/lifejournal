package com.example.lifejournal.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository
    val allEntries: StateFlow<List<JournalEntry>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredEntries: StateFlow<List<JournalEntry>>

    init {
        val db = JournalDatabase.getDatabase(application)
        repository = JournalRepository(db.journalDao())

        allEntries = repository.allEntries
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredEntries = _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) repository.allEntries
                else repository.searchEntries(query)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertEntry(entry: JournalEntry, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertEntry(entry)
            onResult(id)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch { repository.deleteEntry(entry) }
    }

    suspend fun getEntryById(id: Int): JournalEntry? = repository.getEntryById(id)
}