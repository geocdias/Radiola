package com.example.radiola.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radiola.R
import com.example.radiola.data.entities.Song
import kotlinx.android.synthetic.main.item_song.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
  private val glide: RequestManager
): RecyclerView.Adapter<SongAdapter.SongViewHoler>() {

  class SongViewHoler(itemView: View): RecyclerView.ViewHolder(itemView)

  private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
      return oldItem.mediaId == newItem.mediaId
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
      return oldItem.hashCode() == newItem.hashCode()
    }
  }

  private val differ = AsyncListDiffer(this, diffCallback)

  var songs: List<Song>
    get() = differ.currentList
    set(value) = differ.submitList(value)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHoler {
    return SongViewHoler(
      LayoutInflater.from(parent.context).inflate(
        R.layout.item_song,
        parent,
        false
      ))
  }

  override fun onBindViewHolder(holder: SongViewHoler, position: Int) {
    val song = songs[position]
    holder.itemView.apply {
      tvPrimary.text = song.title
      tvSecondary.text = song.artist
      glide.load(song.imageUrl).into(ivItemImage)

      setOnClickListener {
        onItemClickListener?.let { onClick ->
          onClick(song)
        }
      }
    }
  }

  override fun getItemCount(): Int {
    return songs.count()
  }

  private var onItemClickListener: ((Song) -> Unit)? =  null

  fun setOnClickListener(listener: (Song) -> Unit) {
    onItemClickListener = listener
  }
}
