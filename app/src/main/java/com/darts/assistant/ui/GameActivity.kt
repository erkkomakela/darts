package com.darts.assistant.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darts.assistant.R
import com.darts.assistant.databinding.ActivityGameBinding
import com.darts.assistant.db.AppDatabase
import com.darts.assistant.db.GameEntity
import com.darts.assistant.game.*
import kotlinx.coroutines.launch
import java.io.Serializable

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONFIG = "extra_config"
    }

    private lateinit var binding: ActivityGameBinding
    private lateinit var engine: GameEngine
    private lateinit var scoreAdapter: ScoreboardAdapter
    private var multiplier = 1
    private val botHandler = Handler(Looper.getMainLooper())

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val config = intent.getSerializableExtra(EXTRA_CONFIG) as? GameConfig ?: run { finish(); return }
        engine = GameEngine(config)

        setupScoreboard()
        setupNumberPad()
        setupActionButtons()
        updateUI()
    }

    private fun setupScoreboard() {
        scoreAdapter = ScoreboardAdapter(engine.players)
        binding.recyclerScoreboard.layoutManager = LinearLayoutManager(this)
        binding.recyclerScoreboard.adapter = scoreAdapter
    }

    private fun setupNumberPad() {
        val numberButtons = listOf(
            binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btn10,
            binding.btn11, binding.btn12, binding.btn13, binding.btn14, binding.btn15,
            binding.btn16, binding.btn17, binding.btn18, binding.btn19, binding.btn20,
            binding.btn25
        )
        val values = listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,25)

        numberButtons.forEachIndexed { i, btn ->
            btn.setOnClickListener { handleThrow(values[i]) }
        }

        binding.btn0.setOnClickListener { handleThrow(0) }
        binding.btnDouble.setOnClickListener {
            multiplier = if (multiplier == 2) 1 else 2
            updateMultiplierButtons()
        }
        binding.btnTriple.setOnClickListener {
            multiplier = if (multiplier == 3) 1 else 3
            updateMultiplierButtons()
        }
        binding.btnUndo.setOnClickListener { handleUndo() }
    }

    private fun setupActionButtons() {
        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    private fun handleThrow(value: Int) {
        if (!engine.canThrow) return
        if (value == 25 && multiplier == 3) {
            Toast.makeText(this, "Kolmoinen ei mahdollinen bullseye:lle", Toast.LENGTH_SHORT).show()
            return
        }

        val result = engine.throwDart(value, multiplier)
        multiplier = 1
        updateMultiplierButtons()
        updateUI()

        when (result) {
            ThrowResult.BUST -> {
                binding.tvStatus.text = getString(R.string.bust)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red_primary))
                binding.tvStatus.visibility = View.VISIBLE
                updateUI()
                triggerBotIfNeeded()
            }
            ThrowResult.NEXT_PLAYER -> {
                binding.tvStatus.visibility = View.INVISIBLE
                triggerBotIfNeeded()
            }
            ThrowResult.LEG_WON -> showLegWonDialog()
            ThrowResult.SET_WON -> showSetWonDialog()
            ThrowResult.GAME_WON -> showGameWonDialog()
            ThrowResult.CONTINUE -> binding.tvStatus.visibility = View.INVISIBLE
            ThrowResult.NOT_ALLOWED -> {}
        }
    }

    private fun handleUndo() {
        engine.undoLastTurn()
        multiplier = 1
        updateMultiplierButtons()
        updateUI()
    }

    private fun triggerBotIfNeeded() {
        if (!engine.currentPlayer.isBot || engine.gameState != GameState.PLAYING) return
        binding.btnUndo.isEnabled = false
        botHandler.postDelayed({ runBotTurn() }, 800)
    }

    private fun runBotTurn() {
        if (engine.gameState != GameState.PLAYING) return
        val throwData = engine.getBotThrow()
        val result = engine.throwDart(throwData.value, throwData.multiplier)
        updateUI()
        when (result) {
            ThrowResult.CONTINUE -> botHandler.postDelayed({ runBotTurn() }, 600)
            ThrowResult.NEXT_PLAYER, ThrowResult.BUST -> {
                binding.btnUndo.isEnabled = true
                updateUI()
                triggerBotIfNeeded()
            }
            ThrowResult.LEG_WON -> showLegWonDialog()
            ThrowResult.SET_WON -> showSetWonDialog()
            ThrowResult.GAME_WON -> showGameWonDialog()
            else -> binding.btnUndo.isEnabled = true
        }
    }

    private fun updateUI() {
        val player = engine.currentPlayer
        scoreAdapter.setActivePlayer(engine.currentPlayerIndex)
        scoreAdapter.notifyDataSetChanged()

        val hint = engine.getCheckoutHint()
        if (hint != null) {
            binding.tvCheckout.text = "${getString(R.string.checkout_hint)}: $hint"
            binding.tvCheckout.visibility = View.VISIBLE
        } else {
            binding.tvCheckout.visibility = View.INVISIBLE
        }

        val throws = engine.getCurrentThrows()
        binding.tvCurrentThrows.text = throws.joinToString("  ") { it.toString() }.ifEmpty { "" }
        binding.tvTurnScore.text = if (engine.getTurnScore() > 0) "+${engine.getTurnScore()}" else ""
    }

    private fun updateMultiplierButtons() {
        binding.btnDouble.isSelected = multiplier == 2
        binding.btnTriple.isSelected = multiplier == 3
        binding.btnDouble.setBackgroundColor(
            ContextCompat.getColor(this, if (multiplier == 2) R.color.red_primary else R.color.orange_double)
        )
        binding.btnTriple.setBackgroundColor(
            ContextCompat.getColor(this, if (multiplier == 3) R.color.red_primary else R.color.orange_triple)
        )
    }

    private fun showLegWonDialog() {
        val winner = engine.players.maxByOrNull { it.legsWon } ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.leg_won))
            .setMessage("${winner.name} voitti legin!")
            .setPositiveButton("Jatka") { _, _ ->
                engine.continueAfterLegOrSet()
                updateUI()
                triggerBotIfNeeded()
            }
            .setCancelable(false)
            .show()
    }

    private fun showSetWonDialog() {
        val winner = engine.players.maxByOrNull { it.setsWon } ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_won))
            .setMessage("${winner.name} voitti setin!")
            .setPositiveButton("Jatka") { _, _ ->
                engine.continueAfterLegOrSet()
                updateUI()
                triggerBotIfNeeded()
            }
            .setCancelable(false)
            .show()
    }

    private fun showGameWonDialog() {
        val winner = engine.players.maxByOrNull { it.setsWon } ?: return
        saveGame(winner)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_won))
            .setMessage("${winner.name} VOITTI PELIN! \uD83C\uDFAF")
            .setPositiveButton(getString(R.string.new_game)) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun saveGame(winner: com.darts.assistant.game.Player) {
        val summary = engine.players.joinToString(", ") { "${it.name}: avg ${"%.1f".format(it.average)}" }
        val entity = GameEntity(
            gameMode = "X01",
            startingPoints = engine.players.first().totalScored + engine.players.first().score,
            winnerName = winner.name,
            playersSummary = summary,
            durationSeconds = engine.getGameDurationSeconds()
        )
        lifecycleScope.launch {
            AppDatabase.getInstance(applicationContext).gameDao().insertGame(entity)
        }
    }
}
