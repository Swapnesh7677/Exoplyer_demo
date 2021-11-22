package com.swapnesh.exoplyer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar.VISIBLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Util
import com.swapnesh.exoplyer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var track_Selector: DefaultTrackSelector
    private lateinit var audioManager: AudioManager

    private var autoPlay: Boolean = true
    var videoURL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private var mCurrentmilsec: Long = 0
    private var currBrightness = 0
    private var height = 0
    private var width = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkSystemWritePermission()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        height = displaymetrics.heightPixels
        width = displaymetrics.widthPixels


        audioManage()
        brightnessManage()


        binding.idExoPlayer.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN ->
                    if(event.x <(width /2)){
                            //Left
                        if(binding.volmeLayout.visibility== VISIBLE){
                            binding.volmeLayout.visibility = GONE
                        }else{
                            binding.volmeLayout.visibility = VISIBLE
                        }

                    }else if(event.x >(width /2)){
                        //right
                        if(binding.brightnessLayout.visibility== VISIBLE){
                            binding.brightnessLayout.visibility = GONE
                        }else{
                            binding.brightnessLayout.visibility = VISIBLE
                        }


                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    private fun brightnessManage() {
        currBrightness = Settings.System.getInt(contentResolver, SCREEN_BRIGHTNESS, 0)
        binding.seekbarBrightness.max = 255
        binding.seekbarBrightness.keyProgressIncrement = 1
        binding.seekbarBrightness.progress =currBrightness
        binding.seekbarBrightness.setOnSeekBarChangeListener(object :OnSeekBarChangeListener{

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val screenBrightnessValue: Int = p1 * 255 / 100
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(contentResolver, SCREEN_BRIGHTNESS, screenBrightnessValue)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
    }

    private fun audioManage() {

        binding.seekbarVolume.max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekbarVolume.progress= audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekbarVolume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                audioManager.setStreamVolume(exoPlayer.audioStreamType, i, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


    fun releasePlayer(){
         mCurrentmilsec = exoPlayer.getCurrentPosition();
         exoPlayer.release()
         autoPlay = exoPlayer.playWhenReady
     }

     override fun onStop() {
         releasePlayer()
         super.onStop()
     }

     override fun onStart() {
         initializePlayer()
         super.onStart()
     }


    private fun initializePlayer() {
        val resuming = mCurrentmilsec != 0L
        autoPlay = true
        val bandwidthMeter = DefaultBandwidthMeter()
        val extractorsFactory = DefaultExtractorsFactory()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "Exoplyer"), bandwidthMeter as TransferListener<in DataSource>)
        val mediaSource = ExtractorMediaSource(Uri.parse(videoURL),
            mediaDataSourceFactory, extractorsFactory, null, null)
        binding.idExoPlayer?.requestFocus()
        track_Selector = DefaultTrackSelector(videoTrackSelectionFactory)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, track_Selector)
        binding.idExoPlayer.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
        binding.idExoPlayer?.player = exoPlayer
        exoPlayer.playWhenReady = autoPlay

        exoPlayer.prepare(mediaSource,resuming,false)
        if (resuming) {
            exoPlayer.seekTo(mCurrentmilsec);
        }

    }



    private fun checkSystemWritePermission(): Boolean {
        if (Settings.System.canWrite(this)) return true else openAndroidPermissionsMenu()
        return false
    }


    private fun openAndroidPermissionsMenu() {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.getPackageName())
           startActivity(intent)
    }
 }