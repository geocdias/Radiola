package com.example.radiola.player.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.radiola.player.FirebaseMusicSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MusicPlaybackPrepare(
  private val firebaseMusicSource: FirebaseMusicSource,
  private val playerPrepares: (MediaMetadataCompat?) -> Unit
): MediaSessionConnector.PlaybackPreparer {
  override fun onCommand(
    player: Player,
    controlDispatcher: ControlDispatcher,
    command: String,
    extras: Bundle?,
    cb: ResultReceiver?
  ): Boolean  = false

  override fun getSupportedPrepareActions(): Long {
    return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
  }

  override fun onPrepare(playWhenReady: Boolean) = Unit

  override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
    firebaseMusicSource.whenReady {
      val songToPlay = firebaseMusicSource.songs.find { mediaId == it.description.mediaId }
      playerPrepares(songToPlay)
    }
  }

  override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?)  = Unit

  override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}
