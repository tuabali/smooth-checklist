package com.example.smoothchecklist.ui.checklist

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smoothchecklist.data.ChecklistItem
import com.example.smoothchecklist.databinding.ItemChecklistBinding

class ChecklistAdapter(
    private val onCheckedChange: (Long, Boolean) -> Unit,
    private val onTextChange: (Long, String) -> Unit,
    private val onMicClick: (Long) -> Unit,
    private val onDeleteClick: (Long) -> Unit,
    private val onFocusChange: (Long) -> Unit
) : ListAdapter<ChecklistItem, ChecklistAdapter.ChecklistViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val binding = ItemChecklistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChecklistViewHolder(
            binding,
            onCheckedChange,
            onTextChange,
            onMicClick,
            onDeleteClick,
            onFocusChange
        )
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ChecklistViewHolder(
        private val binding: ItemChecklistBinding,
        private val onCheckedChange: (Long, Boolean) -> Unit,
        private val onTextChange: (Long, String) -> Unit,
        private val onMicClick: (Long) -> Unit,
        private val onDeleteClick: (Long) -> Unit,
        private val onFocusChange: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private var currentId: Long = -1L
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (currentId != -1L) {
                    onTextChange(currentId, s?.toString().orEmpty())
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        init {
            binding.itemText.addTextChangedListener(textWatcher)
            binding.itemText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && currentId != -1L) {
                    onFocusChange(currentId)
                }
            }
            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (currentId != -1L) {
                    onCheckedChange(currentId, isChecked)
                }
            }
            binding.micButton.setOnClickListener {
                if (currentId != -1L) {
                    onMicClick(currentId)
                }
            }
            binding.deleteButton.setOnClickListener {
                if (currentId != -1L) {
                    onDeleteClick(currentId)
                }
            }
        }

        fun bind(item: ChecklistItem) {
            currentId = item.id
            if (binding.itemText.text?.toString() != item.text) {
                binding.itemText.setText(item.text)
                binding.itemText.setSelection(binding.itemText.text?.length ?: 0)
            }
            if (binding.itemCheckbox.isChecked != item.isChecked) {
                binding.itemCheckbox.isChecked = item.isChecked
            }
            if (item.canFocus && !binding.itemText.isFocused) {
                binding.itemText.requestFocus()
                binding.itemText.post {
                    binding.itemText.setSelection(binding.itemText.text?.length ?: 0)
                    val inputMethodManager =
                        binding.itemText.context.getSystemService(android.view.inputmethod.InputMethodManager::class.java)
                    inputMethodManager?.showSoftInput(binding.itemText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ChecklistItem>() {
            override fun areItemsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
