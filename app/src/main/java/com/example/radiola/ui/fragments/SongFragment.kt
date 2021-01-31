package com.example.radiola.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.radiola.R
import com.example.radiola.data.entities.Song
import com.example.radiola.player.extentions.toSong
import com.example.radiola.player.isPlaying
import com.example.radiola.ui.viewmodels.MainViewModel
import com.example.radiola.ui.viewmodels.SongViewModel
import com.example.radiola.util.Status
import com.example.radiola.util.Status.SUCCESS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

  @Inject
  lateinit var glide: RequestManager

  private lateinit var mainViewModel: MainViewModel
  private val songViewModel: SongViewModel by viewModels()
  private var curPlayingSong: Song? = null
  private var playbackState: PlaybackStateCompat? = null
  private var shouldUpdateSeekBar = true


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    subscribeToObservers()

    ivPlayPauseDetail.setOnClickListener {
      curPlayingSong?.let {
        mainViewModel.playOrToggleSong(it, true)
      }
    }

    ivSkipPrevious.setOnClickListener { mainViewModel.skipToPrevius() }

    ivSkip.setOnClickListener { mainViewModel.skipToNext() }

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
          tvCurTime.text = formatSongTime(progress.toLong())
        }
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        shouldUpdateSeekBar = false
      }

      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.let {
          mainViewModel.seekTo(it.progress.toLong())
          shouldUpdateSeekBar = true
        }
      }
    })
  }

  private fun updateTitleAndSongImage(song: Song) {
    tvSongName.text = song.title
    glide.load(song.imageUrl).into(ivSongImage)
  }

  private fun subscribeToObservers() {
    mainViewModel.mediaItems.observe(viewLifecycleOwner) {
      it?.let { result ->
        when (result.status) {
          SUCCESS -> {
            result.data?.let { songs ->
              if (curPlayingSong == null && songs.isNotEmpty()) {
                curPlayingSong = songs[0]
                updateTitleAndSongImage(songs[0])
              }
            }
          }
        }
      }
    }

    mainViewModel.currentPlaySong.observe(viewLifecycleOwner) {
      if (it == null) return@observe
      curPlayingSong = it.toSong()
      updateTitleAndSongImage(curPlayingSong!!)
    }

    mainViewModel.playbackState.observe(viewLifecycleOwner) {
      playbackState = it
      ivPlayPauseDetail.setImageResource(
        if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
      )
      seekBar.progress = it?.position?.toInt() ?: 0
    }

    songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) {
      if (shouldUpdateSeekBar) {
        seekBar.progress = it.toInt()
        tvCurTime.text = formatSongTime(it)
      }
    }

    songViewModel.currentSongDuration.observe(viewLifecycleOwner) {
      seekBar.max = it.toInt()
      tvSongDuration.text = formatSongTime(it)
    }
  }

  private fun formatSongTime(time: Long?): String {
    val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    return dateFormat.format(time)
  }
}
