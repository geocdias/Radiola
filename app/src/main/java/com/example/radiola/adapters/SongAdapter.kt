package com.example.radiola.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.radiola.R
import kotlinx.android.synthetic.main.item_song.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
  private val glide: RequestManager
) : BaseSongAdapter(R.layout.item_song) {

  override val differ = AsyncListDiffer(this, diffCallback)

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
}
