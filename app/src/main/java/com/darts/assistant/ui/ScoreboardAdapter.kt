package com.darts.assistant.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.darts.assistant.R
import com.darts.assistant.game.Player

class ScoreboardAdapter(private val players: List<Player>) : RecyclerView.Adapter<ScoreboardAdapter.VH>() {

    private var activeIndex = 0

    fun setActivePlayer(index: Int) { activeIndex = index }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val score: TextView = view.findViewById(R.id.tvScore)
        val average: TextView = view.findViewById(R.id.tvAverage)
        val darts: TextView = view.findViewById(R.id.tvDarts)
        val sets: TextView = view.findViewById(R.id.tvSets)
        val legs: TextView = view.findViewById(R.id.tvLegs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_scoreboard, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val player = players[position]
        val isActive = position == activeIndex
        holder.name.text = player.name
        holder.score.text = player.score.toString()
        holder.average.text = "Ka: ${"%.1f".format(player.average)}"
        holder.darts.text = "Tikkoja: ${player.dartsThrown}"
        holder.sets.text = "S: ${player.setsWon}"
        holder.legs.text = "L: ${player.legsWon}"

        val bg = if (isActive) R.color.player_active else R.color.surface
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, bg))
        holder.score.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
    }

    override fun getItemCount() = players.size
}
