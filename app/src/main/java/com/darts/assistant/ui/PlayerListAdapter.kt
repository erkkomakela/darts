package com.darts.assistant.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darts.assistant.R
import com.darts.assistant.game.Player

class PlayerListAdapter(
    private val players: MutableList<Player>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PlayerListAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvPlayerName)
        val icon: ImageView = view.findViewById(R.id.ivPlayerIcon)
        val delete: ImageButton = view.findViewById(R.id.btnDeletePlayer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.icon.setImageResource(if (player.isBot) R.drawable.ic_bot else R.drawable.ic_person)
        holder.delete.setOnClickListener { onDelete(holder.adapterPosition) }
    }

    override fun getItemCount() = players.size
}
