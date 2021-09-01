package com.example.videoplayer_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.videoplayer_app.databinding.ActivityMainBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

import com.yausername.youtubedl_android.YoutubeDLRequest


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val videoUrl = "https://www.youtube.com/watch?v=yJg-Y5byMMw"
    private val videoId = "yJg-Y5byMMw" //change this video id with your desired video's id from youtube
    //it can be found at the end of youtube video url after v=
    private var player : SimpleExoPlayer? = null
    private var whenReady = true
    private var currentWindow = 0
    private var playBackPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get thumbnail and crop it's black bars
        val url = getString(R.string.get_thumbnail,videoId)
        val handler = ThumbnailHandler()
        handler.getCroppedThumbnail(url,binding.thumbnail,this)
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        binding.videoView.player = player

        //we need to get the instance of YoutubeDl before using it
        //preferably it should be done in onCreate() method
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e("ytdl", "failed to initialize youtubedl-android", e)
        }

        //getting playable url from youtube url for Exoplayer
        val request = YoutubeDLRequest(videoUrl)
        request.addOption("-f", "best")
        val streamInfo = YoutubeDL.getInstance().getInfo(request)

        //setting up exoplayer for the extracted url
        val mediaItem = MediaItem.fromUri(streamInfo.url)
        player?.apply {
            setMediaItem(mediaItem)
            playWhenReady = whenReady
            seekTo(currentWindow,playBackPosition)
            prepare()
            //you can use pause() method after prepare if you don't want it to autoplay
        }

    }

    private fun releasePlayer() {
        //exoplayer use a lot of processing power
        //so it better to destroy the player when no longer in use
        player?.run {
            //here we saved the position of media being played when player was destroyed
            playBackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            whenReady = this.playWhenReady
            release()
        }
        player = null
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        //there's not guarantee of onStop being called when sdk < 24
        //that is why we have to release player in onPause()
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        //for devices with sdk>=24 onStop() is guaranteed to be called
        // that is why we released player here for such devices
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

}