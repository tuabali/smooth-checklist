package com.example.smoothchecklist.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryChecklistRepository @Inject constructor() : ChecklistRepository {
    private val items = mutableListOf<ChecklistItem>()
    private val itemsLiveData = MutableLiveData<List<ChecklistItem>>()
    private var nextId = 1L

    override val checklistItems: LiveData<List<ChecklistItem>> = itemsLiveData

    init {
        itemsLiveData.value = items.toList()
    }

    override fun addItem(): Long {
        val id = nextId++
        items.add(
            ChecklistItem(
                id = id,
                isChecked = false,
                text = ""
            )
        )
        itemsLiveData.value = items.toList()
        return id
    }

    override fun updateText(id: Long, text: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(text = text)
            itemsLiveData.value = items.toList()
        }
    }

    override fun toggleChecked(id: Long, isChecked: Boolean) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(isChecked = isChecked)
            itemsLiveData.value = items.toList()
        }
    }

    override fun deleteItem(id: Long) {
        items.removeAll { it.id == id }
        itemsLiveData.value = items.toList()
    }
}
