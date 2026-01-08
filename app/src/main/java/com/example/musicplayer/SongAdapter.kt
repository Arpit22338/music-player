package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private var songs: List<Song>,
    private val hiddenManager: HiddenManager,
    private val onPlay: (Song) -> Unit,
    private val onLongPress: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.VH>() {

    fun update(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = songs[position]
        holder.title.text = s.title
        holder.subtitle.text = s.artist ?: "Unknown"
        holder.itemView.alpha = if (hiddenManager.isHidden(s.id)) 0.4f else 1.0f
        holder.itemView.setOnClickListener { onPlay(s) }
        holder.itemView.setOnLongClickListener {
            onLongPress(s)
            true
        }
    }

    override fun getItemCount(): Int = songs.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val subtitle: TextView = view.findViewById(R.id.subtitle)
    }
}
