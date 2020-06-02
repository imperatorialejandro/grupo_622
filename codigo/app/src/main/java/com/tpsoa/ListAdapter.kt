package com.tpsoa

import android.R.attr.duration
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.tpsoa.model.VoiceNote
import java.io.File
import java.lang.String
import java.util.concurrent.TimeUnit


class ListAdapter(var cont: Context, var resource: Int, var items: List<VoiceNote>) :
    ArrayAdapter<VoiceNote>(cont, resource, items) {

    //    private lateinit var player: MediaPlayer
//    private lateinit var startTime: TextView
    private lateinit var songTime: TextView

    //    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    // Extension property to get media player duration in seconds
    val MediaPlayer.seconds: Int
        get() {
            return this.duration / 1000
        }

    // Extension property to get media player current position in seconds
    val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition / 1000
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(cont)

        val view: View = layoutInflater.inflate(resource, null)
        var nameTextView = view.findViewById<TextView>(R.id.name)
        var durationTextView = view.findViewById<TextView>(R.id.duration)
        var locationTextView = view.findViewById<TextView>(R.id.location)
        var timeTextView = view.findViewById<TextView>(R.id.time)
        var playBtn = view.findViewById<Button>(R.id.playBtn)

        var seekBar = view.findViewById<SeekBar>(R.id.seekBar)


        var voiceNote: VoiceNote = items[position]

        var file = File(voiceNote.path)

        var player = MediaPlayer()
        player.setDataSource(voiceNote.path)
        player.prepare()

        var duration = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(player.duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(player.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.duration.toLong()))
        )

        nameTextView.text = file.name
        durationTextView.text = duration
        locationTextView.text = voiceNote.location


        player.setOnCompletionListener { player ->
            playBtn.text = "play"
            Log.i("asd", "stoping " + voiceNote.path)
            seekBar.progress = 0
            timeTextView.text = "00:00"
            player.seekTo(0)
            handler.removeCallbacks(runnable)
            player.reset()
            player.setDataSource(voiceNote.path)
            player.prepare()
        }

        playBtn.setOnClickListener {
            Log.i("asd", "---------------------------")
            Log.i("asd", "isPlaying: " + player.isPlaying.toString())
            if (player.isPlaying) {
                Log.i("asd", "pausing " + voiceNote.path)
                playBtn.text = "play"
                player.pause()
            } else {
                Log.i("asd", "playing " + voiceNote.path)
                playBtn.text = "pause"

//                try {
//                    player = MediaPlayer()
//                    player.setDataSource(voiceNote.path)
//                    player.prepare()
//                } catch (e: IllegalArgumentException) {
//                    e.printStackTrace()
//                } catch (e: Exception) {
//                    println("Exception of type : $e")
//                    e.printStackTrace()
//                }
                /////////////////////////////////////////////////
                seekBar.max = player.seconds
                runnable = Runnable {
                    seekBar.progress = player.currentSeconds

                    var current = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(player.currentPosition.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(player.currentPosition.toLong()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.currentPosition.toLong()))
                    )
                    timeTextView.text = current

                    handler.postDelayed(runnable, 1000)
                }
                handler.postDelayed(runnable, 1000)
                /////////////////////////////////////////////////
                player.start()
            }
            Log.i("asd", "isPlaying: " + player.isPlaying.toString())
            Log.i("asd", "---------------------------")
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    player.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        return view
    }
//    private fun initializeSeekBar() {
//        seek_bar.max = player.seconds
//
//        runnable = Runnable {
//            seek_bar.progress = player.currentSeconds
//
//            tv_pass.text = "${player.currentSeconds} sec"
//            val diff = player.seconds - player.currentSeconds
//            tv_due.text = "$diff sec"
//
//            handler.postDelayed(runnable, 1000)
//        }
//        handler.postDelayed(runnable, 1000)
//    }
}