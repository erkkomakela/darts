package com.darts.assistant.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darts.assistant.R
import com.darts.assistant.db.GameEntity
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val onDelete: (GameEntity) -> Unit) :
    ListAdapter<GameEntity, HistoryAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GameEntity>() {
            override fun areItemsTheSame(a: GameEntity, b: GameEntity) = a.id == b.id
            override fun areContentsTheSame(a: GameEntity, b: GameEntity) = a == b
        }
        val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.tvDate)
        val winner: TextView = view.findViewById(R.id.tvWinner)
        val summary: TextView = view.findViewById(R.id.tvSummary)
        val delete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val game = getItem(position)
        holder.date.text = DATE_FORMAT.format(Date(game.date))
        holder.winner.text = "\uD83C\uDFC6 ${game.winnerName}"
        holder.summary.text = "${game.gameMode} ${game.startingPoints} — ${game.playersSummary}"
        holder.delete.setOnClickListener { onDelete(game) }
    }
}
