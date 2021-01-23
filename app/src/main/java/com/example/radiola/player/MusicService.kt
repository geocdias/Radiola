package com.example.radiola.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.radiola.player.callbacks.MusicPlaybackPrepare
import com.example.radiola.player.callbacks.MusicPlayerEventListener
import com.example.radiola.player.callbacks.MusicPlayerNotificationListener
import com.example.radiola.util.Constants.MEDIA_ROOT_ID
import com.example.radiola.util.Constants.NETWORK_ERROR
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_DEBUG_TAG = "Music Service"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {


  @Inject
  lateinit var defaultDataSourceFactory: DefaultDataSourceFactory

  @Inject
  lateinit var exoPlayer: SimpleExoPlayer

  @Inject
  lateinit var firebaseMusicSource: FirebaseMusicSource

  private lateinit var musicNotificationManager: MusicNotificationManager

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

  /*Allows interaction with media controllers, volume keys, media buttons, and transport controls.*/
  private lateinit var mediaSession: MediaSessionCompat
  private lateinit var mediaSessionConnector: MediaSessionConnector
  private lateinit var musicPlayerEventListener: MusicPlayerEventListener

  private var curPlayingSong: MediaMetadataCompat? = null

  var isForegroundService = false
  private var isPlayerInitialized = false

  companion object {
    var currentSongDuration = 0L
      private set
  }

  override fun onCreate() {
    super.onCreate()

    serviceScope.launch {
      firebaseMusicSource.fetchMediaData()
    }

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
      //Update de current duration os de songs
      currentSongDuration = exoPlayer.duration
    }

    val musicPlaybackPreparer = MusicPlaybackPrepare(firebaseMusicSource) {
      curPlayingSong = it
      preparePlayer(firebaseMusicSource.songs, it, true)
    }

    mediaSessionConnector = MediaSessionConnector(mediaSession)
    mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
    mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
    mediaSessionConnector.setPlayer(exoPlayer)

    musicPlayerEventListener = MusicPlayerEventListener(this)

    exoPlayer.addListener(musicPlayerEventListener)

    musicNotificationManager.showNotification(exoPlayer)
  }

  private fun preparePlayer(
    songs: List<MediaMetadataCompat>,
    songToPlay: MediaMetadataCompat?,
    playNow: Boolean
  ) {
    val curlSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(songToPlay)
    exoPlayer.prepare(firebaseMusicSource.asMediaSource(defaultDataSourceFactory))
    exoPlayer.seekTo(curlSongIndex, 0L)
    exoPlayer.playWhenReady = playNow
  }

  override fun onGetRoot(
    clientPackageName: String,
    clientUid: Int,
    rootHints: Bundle?
  ): BrowserRoot? {
    return BrowserRoot(MEDIA_ROOT_ID, null)
  }

  override fun onLoadChildren(
    parentId: String,
    result: Result<MutableList<MediaBrowserCompat.MediaItem>>
  ) {
    when (parentId) {
      MEDIA_ROOT_ID -> {
        val resultSend = firebaseMusicSource.whenReady { isInitialized ->
          if (isInitialized) {
            result.sendResult(firebaseMusicSource.asMediaItems())
            if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
              preparePlayer(
                firebaseMusicSource.songs,
                firebaseMusicSource.songs[0],
                false
              )
              isPlayerInitialized = true
            }
          } else {
            mediaSession.sendSessionEvent(NETWORK_ERROR, null)
            result.sendResult(null)
          }
        }
        if (!resultSend) {
          result.detach()
        }
      }
    }
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    exoPlayer.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    exoPlayer.removeListener(musicPlayerEventListener)
    exoPlayer.release()
  }

  private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
      return firebaseMusicSource.songs[windowIndex].description
    }
  }
}
