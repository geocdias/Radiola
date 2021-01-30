package com.example.radiola.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radiola.R
import com.example.radiola.adapters.SongAdapter
import com.example.radiola.ui.viewmodels.MainViewModel
import com.example.radiola.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

  lateinit var mainViewModel: MainViewModel

  @Inject
  lateinit var songAdapter: SongAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    setupRecyclerView()
    subscribeToObservers()

    songAdapter.setOnClickListener { song ->
      println("Play the song $song")
      mainViewModel.playOrToggleSong(song)
    }
  }

  private fun setupRecyclerView() = rvAllSongs.apply {
    adapter = songAdapter
    layoutManager = LinearLayoutManager(requireContext())
  }

  private fun subscribeToObservers() {
    mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
      when(result.status) {
        Status.SUCCESS -> {
          allSongsProgressBar.isVisible = false
           result.data?.let { songs ->
             songAdapter.songs = songs
           }
        }

        Status.ERROR -> Unit

        Status.LOADING -> {
          allSongsProgressBar.isVisible = true
        }
      }

    }
  }

}
