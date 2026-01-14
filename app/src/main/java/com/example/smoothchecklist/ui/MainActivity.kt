package com.example.smoothchecklist.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smoothchecklist.R
import com.example.smoothchecklist.databinding.ActivityMainBinding
import com.example.smoothchecklist.ui.checklist.ChecklistAdapter
import com.example.smoothchecklist.ui.checklist.ChecklistViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChecklistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ChecklistAdapter(
            onCheckedChange = { id, isChecked -> viewModel.toggleChecked(id, isChecked) },
            onTextChange = { id, text -> viewModel.updateText(id, text) },
            onMicClick = {
                Toast.makeText(
                    this,
                    getString(R.string.mic_placeholder),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { id -> viewModel.deleteItem(id) }
        )

        binding.ivTick.setOnClickListener {
            val newId = viewModel.addItem()
            adapter.requestFocusFor(newId)
            lifecycleScope.launch {
                delay(300)
                binding.checklistRecycler.scrollToPosition(adapter.currentList.size - 1)
            }
        }

        binding.checklistRecycler.layoutManager = LinearLayoutManager(this)
        binding.checklistRecycler.adapter = adapter
        binding.checklistRecycler.setHasFixedSize(true)
        binding.checklistRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val focusedId = adapter.getFocusedItemId() ?: return
                    val focusedHolder = recyclerView.findViewHolderForItemId(focusedId)
                        ?: return
                    focusedHolder.itemView.findViewById<android.widget.EditText>(
                        com.example.smoothchecklist.R.id.itemText
                    )?.requestFocus()
                }
            }
        })
        binding.titleInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adapter.clearFocusedItem()
            }
        }
        viewModel.checklistItems.observe(this) { items ->
            adapter.submitList(items)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val contentPadding = resources.getDimensionPixelSize(R.dimen.screen_padding)
            val bottomInset = max(systemBars.bottom, imeInsets.bottom)
            binding.toolbar.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.contentContainer.setPadding(
                contentPadding + systemBars.left,
                contentPadding,
                contentPadding + systemBars.right,
                contentPadding + bottomInset
            )
            insets
        }

    }
}
