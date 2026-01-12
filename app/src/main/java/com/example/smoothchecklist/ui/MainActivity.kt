package com.example.smoothchecklist.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smoothchecklist.data.ChecklistItem
import com.example.smoothchecklist.databinding.ActivityMainBinding
import com.example.smoothchecklist.ui.checklist.ChecklistAdapter
import com.example.smoothchecklist.ui.checklist.ChecklistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChecklistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setOnMenuItemClickListener { menuItem ->
//            if (menuItem.itemId == com.example.smoothchecklist.R.id.action_done) {
//                viewModel.addItem()
//                true
//            } else {
//                false
//            }
//        }

        binding.ivTick.setOnClickListener {
            viewModel.addItem()
        }

        val adapter = ChecklistAdapter(
            onCheckedChange = { id, isChecked -> viewModel.toggleChecked(id, isChecked) },
            onTextChange = { id, text -> viewModel.updateText(id, text) },
            onMicClick = {
                Toast.makeText(this, getString(com.example.smoothchecklist.R.string.mic_placeholder), Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { id -> viewModel.deleteItem(id) }
        )

        binding.checklistRecycler.layoutManager = LinearLayoutManager(this)
        binding.checklistRecycler.adapter = adapter
        binding.checklistRecycler.setHasFixedSize(true)
        val list = arrayListOf<ChecklistItem>(ChecklistItem(1L, false, "test one"))
        adapter.submitList(list)
        viewModel.checklistItems.observe(this) { items ->
            adapter.submitList(items)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}
