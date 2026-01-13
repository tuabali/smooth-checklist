package com.example.smoothchecklist.data

import androidx.lifecycle.LiveData

interface ChecklistRepository {
    val checklistItems: LiveData<List<ChecklistItem>>

    fun addItem(): Long
    fun updateText(id: Long, text: String)
    fun toggleChecked(id: Long, isChecked: Boolean)
    fun deleteItem(id: Long)
}
