package com.darts.assistant.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darts.assistant.R
import com.darts.assistant.databinding.ActivityHistoryBinding
import com.darts.assistant.db.AppDatabase
import com.darts.assistant.db.GameEntity
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter { game -> confirmDelete(game) }
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnDeleteAll.setOnClickListener { confirmDeleteAll() }

        lifecycleScope.launch {
            db.gameDao().getAllGames().collect { games ->
                adapter.submitList(games)
                binding.tvEmpty.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmDelete(game: GameEntity) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                lifecycleScope.launch { db.gameDao().deleteGame(game) }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_all_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                lifecycleScope.launch { db.gameDao().deleteAllGames() }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
