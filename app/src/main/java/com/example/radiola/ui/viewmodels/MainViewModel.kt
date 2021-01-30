package com.example.radiola.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.radiola.data.entities.Song
import com.example.radiola.player.MusicServiceConnection
import com.example.radiola.player.isPlayEnable
import com.example.radiola.player.isPlaying
import com.example.radiola.player.isPrepared
import com.example.radiola.util.Constants.MEDIA_ROOT_ID
import com.example.radiola.util.Resource
import com.example.radiola.util.Resource.Companion.loading

class MainViewModel @ViewModelInject constructor(
  private val musicServiceConnection: MusicServiceConnection
):ViewModel() {

  private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
  val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

  val isConnected = musicServiceConnection.isConnected
  val networkError = musicServiceConnection.networkError
  val currentPlaySong = musicServiceConnection.currentPlayingSong
  val playbackState = musicServiceConnection.playbackState

  init {
    _mediaItems.postValue(loading(null))
    musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {
      override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
      ) {
        super.onChildrenLoaded(parentId, children)
        val songList = children.map {
          Song(
            it.mediaId!!,
            it.description.title.toString(),
            it.description.subtitle.toString(),
            it.description.mediaUri.toString(),
            it.description.iconUri.toString()
          )
        }
        _mediaItems.postValue(Resource.success(songList))
      }
    })
  }

  fun skipToNext() {
    musicServiceConnection.transportControls.skipToNext()
  }

  fun skipToPrevius() {
    musicServiceConnection.transportControls.skipToPrevious()
  }

  fun seekTo(pos: Long) {
    musicServiceConnection.transportControls.seekTo(pos)
  }

  fun playOrToggleSong(song: Song, toggle: Boolean = false) {
    val isPrepared = playbackState.value?.isPrepared ?: false
    if(isPrepared && song.mediaId == currentPlaySong.value?.getString(METADATA_KEY_MEDIA_ID)) {
      playbackState.value?.let { playbackState ->
        when {
          playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
          playbackState.isPlayEnable ->  musicServiceConnection.transportControls.play()
          else -> Unit
        }
      }
    } else {
      musicServiceConnection.transportControls.playFromMediaId(song.mediaId, null)
    }
  }

  override fun onCleared() {
    super.onCleared()
    musicServiceConnection.unSubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
  }

}
