package com.example.radiola.player

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.radiola.player.callbacks.MusicPlaybackPrepare
import com.example.radiola.player.callbacks.MusicPlayerEventListener
import com.example.radiola.player.callbacks.MusicPlayerNotificationListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

private const val SERVICE_DEBUG_TAG = "Music Service"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

  @Inject
  lateinit var defaultDataSourceFactory: DefaultDataSourceFactory

  @Inject
  lateinit var exoPlayer: SimpleExoPlayer

  @Inject
  lateinit var firebaseMusicSource: FirebaseMusicSource

  private lateinit var musicNotificationManager: MusicNotificationManager

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

  private lateinit var mediaSession: MediaSessionCompat
  private lateinit var mediaSessionConnector: MediaSessionConnector

  private var curPlayingSong: MediaMetadataCompat? = null

  var isForegroundService = false

  override fun onCreate() {
    super.onCreate()
    val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
      PendingIntent.getActivity(this, 0, it, 0)
    }

    mediaSession = MediaSessionCompat(this, SERVICE_DEBUG_TAG).apply {
      setSessionActivity(activityIntent)
      isActive = true
    }

    sessionToken = mediaSession.sessionToken

    musicNotificationManager = MusicNotificationManager(
      this,
      mediaSession.sessionToken,
      MusicPlayerNotificationListener(this)
    ) {

    }

    val musicPlaybackPreparer = MusicPlaybackPrepare(firebaseMusicSource) {
      curPlayingSong = it
      preparePlayer(firebaseMusicSource.songs, it, true)
    }

    mediaSessionConnector = MediaSessionConnector(mediaSession)
    mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
    mediaSessionConnector.setPlayer(exoPlayer)

    exoPlayer.addListener(MusicPlayerEventListener(this))

    musicNotificationManager.showNotification(exoPlayer)
  }

  private fun preparePlayer(
    songs: List<MediaMetadataCompat>,
    songToPlay: MediaMetadataCompat?,
    playNow: Boolean
  ) {
    val curlSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(songToPlay)
    exoPlayer.prepare(firebaseMusicSource.asMediaSource(defaultDataSourceFactory))
    exoPlayer.seekTo(curlSongIndex, 0L)
    exoPlayer.playWhenReady = playNow
  }

  override fun onGetRoot(
    clientPackageName: String,
    clientUid: Int,
    rootHints: Bundle?
  ): BrowserRoot? {
    TODO("Not yet implemented")
  }

  override fun onLoadChildren(
    parentId: String,
    result: Result<MutableList<MediaBrowserCompat.MediaItem>>
  ) {
    TODO("Not yet implemented")
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }
}
