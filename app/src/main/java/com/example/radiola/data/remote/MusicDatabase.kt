package com.example.radiola.data.remote

import com.example.radiola.data.entities.Song
import com.example.radiola.util.Constants.SONGS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection(SONGS_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            songsCollection.get().await().toObjects(Song::class.java)
        }catch (e: Exception) {
            return emptyList()
        }
    }
}
