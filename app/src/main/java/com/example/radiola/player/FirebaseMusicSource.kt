package com.example.radiola.player

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.radiola.data.remote.MusicDatabase
import com.example.radiola.player.State.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(private val musicDatabase: MusicDatabase) {

   var songs = emptyList<MediaMetadataCompat>()

  /**
   * Fetch all songs from firebase and convert to  MediaMetadataCompat that contains
   * meta information about the song
   **/
  suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
    state = INITIALIZING

    val allSongs = musicDatabase.getAllSongs()
    songs = allSongs.map { song ->
      MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_ARTIST, song.artist)
        .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
        .putString(METADATA_KEY_TITLE, song.title)
        .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
        .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artist)
        .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.artist)
        .build()
    }
    state = INITIALIZED
  }

  /**
   * Contains information where de player can stream de song
   **/
  fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
    val concatenatingMediaSource = ConcatenatingMediaSource()
    songs.forEach { song ->
      val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
      concatenatingMediaSource.addMediaSource(mediaSource)
    }

    return concatenatingMediaSource
  }

  fun asMediaItems() = songs.map { song ->
    val mediaDescription = MediaDescriptionCompat.Builder()
      .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
      .setTitle(song.description.title)
      .setSubtitle(song.description.subtitle)
      .setMediaId(song.description.mediaId)
      .setIconUri(song.description.iconUri)
      .build()

    MediaBrowserCompat.MediaItem(mediaDescription, FLAG_PLAYABLE)
  }.toMutableList()

  private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

  private var state: State = CREATED
    set(value) {
      if(value == INITIALIZED || value == ERROR) {
        synchronized(onReadyListeners) {
          field = value
          onReadyListeners.forEach {listener ->
            listener(state == INITIALIZED)
          }
        }
      } else {
        field = value
      }
    }

  fun whenReady(action: (Boolean) -> Unit): Boolean {
    if(state == CREATED || state == INITIALIZING) {
      onReadyListeners += action
      return false
    } else {
      action(state == INITIALIZED)
      return true
    }
  }
}

enum class State {
  CREATED,
  INITIALIZING,
  INITIALIZED,
  ERROR
}
