package com.darts.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darts.assistant.R
import com.darts.assistant.databinding.ActivityMainBinding
import com.darts.assistant.game.GameConfig
import com.darts.assistant.game.Player
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val playerList = mutableListOf<Player>()
    private lateinit var playerAdapter: PlayerListAdapter

    private var selectedPoints = 501
    private var selectedSets = 1
    private var selectedLegs = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPointChips()
        setupSetLegChips()
        setupPlayerList()
        setupButtons()
    }

    private fun setupPointChips() {
        listOf(301, 501, 701).forEachIndexed { i, pts ->
            val chip = binding.chipGroupPoints.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.text = pts.toString()
            chip.setOnClickListener {
                selectedPoints = pts
                updatePointChips()
            }
        }
        updatePointChips()
    }

    private fun updatePointChips() {
        listOf(301, 501, 701).forEachIndexed { i, pts ->
            val chip = binding.chipGroupPoints.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.isChecked = pts == selectedPoints
        }
    }

    private fun setupSetLegChips() {
        listOf(1, 2, 3, 5).forEachIndexed { i, sets ->
            val chip = binding.chipGroupSets.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.text = sets.toString()
            chip.setOnClickListener { selectedSets = sets; updateSetChips() }
        }
        listOf(1, 3, 5, 7).forEachIndexed { i, legs ->
            val chip = binding.chipGroupLegs.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.text = legs.toString()
            chip.setOnClickListener { selectedLegs = legs; updateLegChips() }
        }
        updateSetChips()
        updateLegChips()
    }

    private fun updateSetChips() {
        listOf(1, 2, 3, 5).forEachIndexed { i, sets ->
            val chip = binding.chipGroupSets.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.isChecked = sets == selectedSets
        }
    }

    private fun updateLegChips() {
        listOf(1, 3, 5, 7).forEachIndexed { i, legs ->
            val chip = binding.chipGroupLegs.getChildAt(i) as? Chip ?: return@forEachIndexed
            chip.isChecked = legs == selectedLegs
        }
    }

    private fun setupPlayerList() {
        playerAdapter = PlayerListAdapter(playerList) { pos -> playerList.removeAt(pos); playerAdapter.notifyItemRemoved(pos) }
        binding.recyclerPlayers.layoutManager = LinearLayoutManager(this)
        binding.recyclerPlayers.adapter = playerAdapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean {
                val from = vh.adapterPosition
                val to = t.adapterPosition
                playerList.add(to, playerList.removeAt(from))
                playerAdapter.notifyItemMoved(from, to)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.recyclerPlayers)
    }

    private fun setupButtons() {
        binding.btnAddPlayer.setOnClickListener { showAddPlayerDialog(isBot = false) }
        binding.btnAddBot.setOnClickListener { addBot() }
        binding.btnStartGame.setOnClickListener { startGame() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun showAddPlayerDialog(isBot: Boolean) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_player, null)
        val editName = view.findViewById<EditText>(R.id.editPlayerName)
        AlertDialog.Builder(this)
            .setTitle(if (isBot) getString(R.string.bot) else getString(R.string.add_player))
            .setView(view)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val name = editName.text.toString().trim().ifEmpty { if (isBot) getString(R.string.bot) else "Pelaaja ${playerList.size + 1}" }
                playerList.add(Player(name = name, isBot = isBot))
                playerAdapter.notifyItemInserted(playerList.size - 1)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun addBot() {
        val botCount = playerList.count { it.isBot }
        playerList.add(Player(name = "Botti ${if (botCount == 0) "" else (botCount + 1)}", isBot = true))
        playerAdapter.notifyItemInserted(playerList.size - 1)
    }

    private fun startGame() {
        if (playerList.isEmpty()) {
            Toast.makeText(this, "Lisää ainakin yksi pelaaja", Toast.LENGTH_SHORT).show()
            return
        }
        val config = GameConfig(
            startingPoints = selectedPoints,
            sets = selectedSets,
            legs = selectedLegs,
            players = playerList.toList()
        )
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_CONFIG, config)
        startActivity(intent)
    }
}
