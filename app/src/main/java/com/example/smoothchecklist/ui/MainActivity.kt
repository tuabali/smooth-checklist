package com.example.smoothchecklist.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smoothchecklist.databinding.ActivityMainBinding
import com.example.smoothchecklist.ui.checklist.ChecklistAdapter
import com.example.smoothchecklist.ui.checklist.ChecklistViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChecklistViewModel by viewModels()
    private var pendingSpeechId: Long? = null
    private var pendingFocusId: Long? = null

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val itemId = pendingSpeechId ?: return@registerForActivityResult
        pendingSpeechId = null
        if (result.resultCode == RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) {
                viewModel.updateText(itemId, text)
            }
        }
    }

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingSpeechId?.let { launchSpeechRecognizer(it) }
        } else {
            Toast.makeText(this, getString(com.example.smoothchecklist.R.string.mic_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val adapter = ChecklistAdapter(
            onCheckedChange = { id, isChecked -> viewModel.toggleChecked(id, isChecked) },
            onTextChange = { id, text -> viewModel.updateText(id, text) },
            onMicClick = { id -> handleMicClick(id) },
            onDeleteClick = { id -> viewModel.deleteItem(id) }
        )

        binding.checklistRecycler.layoutManager = LinearLayoutManager(this)
        binding.checklistRecycler.adapter = adapter
        binding.checklistRecycler.setHasFixedSize(true)

        binding.ivTick.setOnClickListener {
            val newId = viewModel.addItem()
            pendingFocusId = newId
            adapter.markNewItem(newId)
        }

        viewModel.checklistItems.observe(this) { items ->
            val focusId = pendingFocusId
            adapter.submitList(items) {
                focusId?.let { id ->
                    val position = items.indexOfFirst { it.id == id }
                    if (position != -1) {
                        focusAndScroll(binding.checklistRecycler, adapter, id, position)
                    }
                }
            }
            pendingFocusId = null
        }

        val contentPaddingStart = binding.contentContainer.paddingStart
        val contentPaddingTop = binding.contentContainer.paddingTop
        val contentPaddingEnd = binding.contentContainer.paddingEnd
        val contentPaddingBottom = binding.contentContainer.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.updatePadding(top = systemBars.top)
            binding.contentContainer.updatePadding(
                left = contentPaddingStart + systemBars.left,
                top = contentPaddingTop,
                right = contentPaddingEnd + systemBars.right,
                bottom = contentPaddingBottom + systemBars.bottom
            )
            insets
        }
    }

    private fun focusAndScroll(
        recyclerView: RecyclerView,
        adapter: ChecklistAdapter,
        id: Long,
        position: Int
    ) {
        recyclerView.post {
            recyclerView.scrollToPosition(position)
            recyclerView.post {
                adapter.focusOnItem(recyclerView, id)
            }
        }
    }

    private fun handleMicClick(id: Long) {
        pendingSpeechId = id
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchSpeechRecognizer(id)
        } else {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun launchSpeechRecognizer(id: Long) {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, getString(com.example.smoothchecklist.R.string.mic_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        pendingSpeechId = id
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(com.example.smoothchecklist.R.string.mic_prompt))
        }
        speechLauncher.launch(intent)
    }
}
