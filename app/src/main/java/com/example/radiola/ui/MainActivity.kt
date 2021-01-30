package com.example.radiola.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.radiola.R
import com.example.radiola.adapters.SwipeSongAdapter
import com.example.radiola.data.entities.Song
import com.example.radiola.player.extentions.toSong
import com.example.radiola.player.isPlaying
import com.example.radiola.ui.viewmodels.MainViewModel
import com.example.radiola.util.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: MainViewModel by viewModels()

  @Inject
  lateinit var glide: RequestManager

  @Inject
  lateinit var swipeSongAdapter: SwipeSongAdapter

  private var currentPlayingSong: Song? = null

  private var playbackState: PlaybackStateCompat? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    vpSong.adapter = swipeSongAdapter

    vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        if(playbackState?.isPlaying == true) {
          viewModel.playOrToggleSong(swipeSongAdapter.songs[position])
        } else {
          currentPlayingSong = swipeSongAdapter.songs[position]
        }
      }
    })

    ivPlayPause.setOnClickListener {
      currentPlayingSong?.let {
        viewModel.playOrToggleSong(it, true)
      }
    }

    subcribeToObservers()
  }

  private fun switchViewPagerToCurrentSong(song: Song) {
    val newItemIndex = swipeSongAdapter.songs.indexOf(song)
    if (newItemIndex != -1) {
      vpSong.currentItem = newItemIndex
      currentPlayingSong = song
    }
  }

  private fun subcribeToObservers() {
    viewModel.mediaItems.observe(this) {
      it?.let { result ->
        when (result.status) {
          Status.SUCCESS -> {
            result.data?.let { songs ->
              swipeSongAdapter.songs = songs
              if(songs.isNotEmpty()) {
                glide.load((currentPlayingSong ?: songs[0]).imageUrl).into(ivCurSongImage)
              }
              switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
            }
          }
          Status.LOADING -> Unit
          Status.ERROR -> Unit
        }
      }
    }

    viewModel.currentPlaySong.observe(this) {
      if(it == null) return@observe

      currentPlayingSong = it.toSong()
      glide.load(currentPlayingSong?.imageUrl).into(ivCurSongImage)

      switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
    }

    viewModel.playbackState.observe(this) {
      playbackState = it
      ivPlayPause.setImageResource(
        if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
      )
      glide.load(currentPlayingSong?.imageUrl).into(ivCurSongImage)
    }

    viewModel.isConnected.observe(this) {
      it?.getContentIfNotHandled()?.let { result ->
        when(result.status) {
          Status.ERROR -> Snackbar.make(
            rootLayout,
            result.message ?: "Ops! Aconteceu algo errado.",
            Snackbar.LENGTH_SHORT
          ).show()
        }
      }
    }

    viewModel.networkError.observe(this) {
      it?.getContentIfNotHandled()?.let { result ->
        when(result.status) {
          Status.ERROR -> Snackbar.make(
            rootLayout,
            result.message ?: "Ops! Aconteceu um erro de conexão",
            Snackbar.LENGTH_SHORT
          ).show()
        }
      }
    }
  }
}
