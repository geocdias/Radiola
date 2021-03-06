package com.example.radiola.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.radiola.R
import com.example.radiola.adapters.SwipeSongAdapter
import com.example.radiola.player.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

  @Singleton
  @Provides
  fun provideGlideInstance(@ApplicationContext context: Context) =
    Glide.with(context).setDefaultRequestOptions(
      RequestOptions()
        .placeholder(R.drawable.ic_launcher_foreground)
        .error(R.drawable.ic_launcher_foreground)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

  @Singleton
  @Provides
  fun provideMusicServiceConnection(@ApplicationContext context: Context) =
    MusicServiceConnection(context)

  @Singleton
  @Provides
  fun provideSwipeSongAdapter() = SwipeSongAdapter()
}
