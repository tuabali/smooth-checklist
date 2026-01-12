package com.example.smoothchecklist.ui.checklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.smoothchecklist.data.ChecklistItem
import com.example.smoothchecklist.data.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val repository: ChecklistRepository
) : ViewModel() {
    val checklistItems: LiveData<List<ChecklistItem>> = repository.checklistItems

    fun addItem() {
        repository.addItem()
    }

    fun updateText(id: Long, text: String) {
        repository.updateText(id, text)
    }

    fun toggleChecked(id: Long, isChecked: Boolean) {
        repository.toggleChecked(id, isChecked)
    }

    fun deleteItem(id: Long) {
        repository.deleteItem(id)
    }
}
