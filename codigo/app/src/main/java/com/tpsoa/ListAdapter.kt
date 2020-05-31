package com.tpsoa

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.tpsoa.model.VoiceNote
import android.os.Handler


class ListAdapter(var cont: Context, var resource: Int, var items: List<VoiceNote>) :
    ArrayAdapter<VoiceNote>(cont, resource, items) {

    //    private lateinit var player: MediaPlayer
    private lateinit var startTime: TextView
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
        var locationTextView = view.findViewById<TextView>(R.id.location)
        var playBtn = view.findViewById<Button>(R.id.playBtn)

        var seekBar = view.findViewById<SeekBar>(R.id.seekBar)

        startTime = view.findViewById(R.id.txtStartTime)
        songTime = view.findViewById(R.id.txtSongTime)

        var voiceNote: VoiceNote = items[position]

        nameTextView.text = voiceNote.name + " " + voiceNote.duration.time
        locationTextView.text = "location"

        var player = MediaPlayer()

        player.setOnCompletionListener { player ->
            playBtn.text = "play"
            Log.i("asd", "stoping " + voiceNote.path)
            player.reset()
            seekBar.progress = 0
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

                try {
//                    player = MediaPlayer()
                    player.setDataSource(voiceNote.path)
                    player.prepare()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    println("Exception of type : $e")
                    e.printStackTrace()
                }
                /////////////////////////////////////////////////
                seekBar.max = player.seconds
                runnable = Runnable {
                    seekBar.progress = player.currentSeconds

                    startTime.text = "${player.currentSeconds} sec"
                    val diff = player.seconds - player.currentSeconds
                    songTime.text = "$diff sec"

                    handler.postDelayed(runnable, 1000)
                }
                handler.postDelayed(runnable, 1000)
                /////////////////////////////////////////////////
                player.start()
            }
            Log.i("asd", "isPlaying: " + player.isPlaying.toString())
            Log.i("asd", "---------------------------")
        }

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