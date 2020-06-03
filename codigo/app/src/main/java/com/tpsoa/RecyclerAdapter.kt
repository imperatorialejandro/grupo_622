package com.tpsoa

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tpsoa.model.VoiceNote
import java.io.File
import java.lang.String
import java.util.concurrent.TimeUnit

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var voiceNotes: MutableList<VoiceNote> = ArrayList()
    private lateinit var context: Context

    fun RecyclerAdapter(voiceNotes: MutableList<VoiceNote>, context: Context) {
        this.voiceNotes = voiceNotes
        this.context = context
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = voiceNotes.get(position)
        holder.bind(item, context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item, parent, false))
    }

    override fun getItemCount(): Int {
        return voiceNotes.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById(R.id.name) as TextView
        val durationTextView = view.findViewById(R.id.duration) as TextView
        val locationTextView = view.findViewById(R.id.location) as TextView
        val timeTextView = view.findViewById(R.id.time) as TextView
        val playBtn = view.findViewById(R.id.playBtn) as Button
        val seekBar = view.findViewById(R.id.seekBar) as SeekBar

        private lateinit var runnable: Runnable
        private var handler: Handler = Handler()

        var mediaPlayer = MediaPlayer()

        fun bind(voiceNote: VoiceNote, context: Context) {
            var file = File(voiceNote.path)

            mediaPlayer.setDataSource(voiceNote.path)
            mediaPlayer.prepareAsync()

            nameTextView.text = file.name
            locationTextView.text = voiceNote.location
            mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener {
                var duration = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.duration.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.duration.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.duration.toLong()))
                )

                durationTextView.text = duration

                playBtn.isEnabled = true
            })

            playBtn.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    playBtn.background = ContextCompat.getDrawable(context, R.drawable.ic_pause_btn)

                    seekBar.max = mediaPlayer.duration / 1000
                    runnable = Runnable {
                        seekBar.progress = mediaPlayer.currentPosition / 1000

                        var current = String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.currentPosition.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.currentPosition.toLong()) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            mediaPlayer.currentPosition.toLong()
                                        )
                                    )
                        )
                        timeTextView.text = current

                        handler.postDelayed(runnable, 1000)
                    }
                    handler.postDelayed(runnable, 1000)

                    mediaPlayer.start()
                }
            }

            mediaPlayer.setOnCompletionListener {
                seekBar.progress = 0
                timeTextView.text = "00:00"
                mediaPlayer.seekTo(0)
                handler.removeCallbacks(runnable)
                mediaPlayer.stop()

                mediaPlayer.reset()
                mediaPlayer.setDataSource(voiceNote.path)
                mediaPlayer.prepareAsync()
            }
        }
    }
}