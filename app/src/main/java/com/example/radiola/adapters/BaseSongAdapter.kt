package com.example.radiola.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.radiola.R
import com.example.radiola.data.entities.Song
import kotlinx.android.synthetic.main.item_song.view.*

abstract class BaseSongAdapter(
  private val layoutId: Int
): RecyclerView.Adapter<BaseSongAdapter.SongViewHoler>() {

  class SongViewHoler(itemView: View): RecyclerView.ViewHolder(itemView)

  protected abstract val differ: AsyncListDiffer<Song>

  var songs: List<Song>
    get() = differ.currentList
    set(value) = differ.submitList(value)

  protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
      return oldItem.mediaId == newItem.mediaId
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
      return oldItem.hashCode() == newItem.hashCode()
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHoler {
    return SongViewHoler(
      LayoutInflater.from(parent.context).inflate(
        layoutId,
        parent,
        false
      ))
  }

  override fun getItemCount(): Int {
    return songs.count()
  }

  protected var onItemClickListener: ((Song) -> Unit)? =  null

  fun setOnClickListener(listener: (Song) -> Unit) {
    onItemClickListener = listener
  }
}
