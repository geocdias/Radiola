package com.example.radiola.player

import com.example.radiola.player.State.*

class FirebaseMusicSource {
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
