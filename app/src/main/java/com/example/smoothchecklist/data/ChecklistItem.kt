package com.example.smoothchecklist.data

data class ChecklistItem(
    val id: Long,
    val isChecked: Boolean,
    val text: String,
    val canFocus: Boolean = false
)
