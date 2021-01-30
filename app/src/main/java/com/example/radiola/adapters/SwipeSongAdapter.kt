package com.example.radiola.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.radiola.R
import kotlinx.android.synthetic.main.item_song.view.*

class SwipeSongAdapter: BaseSongAdapter(R.layout.swipe_item) {

  override val differ = AsyncListDiffer(this, diffCallback)

  override fun onBindViewHolder(holder: SongViewHoler, position: Int) {
    val song = songs[position]

    holder.itemView.apply {
      val text =  "${song.title} - ${song.artist}"
      tvPrimary.text = text

      setOnClickListener {
        onItemClickListener?.let { onClick ->
          onClick(song)
        }
      }
    }
  }
}
