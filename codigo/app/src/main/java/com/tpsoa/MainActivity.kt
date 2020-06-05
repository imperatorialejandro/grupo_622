package com.tpsoa

import android.Manifest
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tpsoa.common.GpsUtils
import com.google.gson.Gson
import com.tpsoa.model.EventRequest
import com.tpsoa.model.VoiceNote
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class MainActivity : BaseActivity() {

    private var accelerometerSensor: Sensor? = null
    private val SHAKE_THRESHOLD: Float = 30.25f
    private val MIN_TIME_BETWEEN_SHAKES = 1000
    private var lastShakeTime: Long = 0

    private lateinit var mediaRecorder: MediaRecorder
    private var audioFilePath: String? = null

    private val formatter = SimpleDateFormat("EEE dd MMMM yyyy, HHmmss")

    private var recordedVoiceNotes: MutableSet<String>? = null

    private var gson = Gson()

    private lateinit var recyclerView: RecyclerView
    private val adapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var outputDirectory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLogged()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes()

        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no accelerometer sensor", Toast.LENGTH_SHORT).show()
        }
        if (checkPermissions(PERMISSIONS)) {
            init()
        }
    }

    override fun init() {
        super.init()
        setupMediaRecorder()
        updateListView()
    }

    private fun checkLogged() {
        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        var loggedUser = SharedPreferencesManager.getUserLogged()
        email_user_text.text = "Hello $loggedUser!"
    }

    private fun isLogged(): Boolean {
        return SharedPreferencesManager.getUserLogged() != ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.voice_recorder_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_item_menu -> {
                onLogoutClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLogoutClick() {
        SharedPreferencesManager.clearCurrentUserPrefs()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun accelerometerSensorTriggered(event: SensorEvent) {
        val curTime = System.currentTimeMillis()
        if (curTime - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt(
                x.toDouble().pow(2.0) +
                        y.toDouble().pow(2.0) +
                        z.toDouble().pow(2.0)
            ) - SensorManager.GRAVITY_EARTH
            if (acceleration > SHAKE_THRESHOLD) {
                lastShakeTime = curTime
                if (isRecording) {
                    stop()
                } else {
                    record()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this,
            this.accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        );
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    fun onStopClick(view: View) {
        stop()
    }

    private fun stop() {
        stopBtn.visibility = View.GONE
        pauseBtn.visibility = View.GONE
        resumeBtn.visibility = View.GONE
        opacityFilter.visibility = View.GONE

        if (isRecording) {
            mediaRecorder.stop()
            mediaRecorder.reset()
            isRecording = false
        }
        recordBtn.visibility = View.VISIBLE

        GpsUtils.getLocation(this) { location ->
            var v = audioFilePath?.let { VoiceNote(it, location) }
            var json = gson.toJson(v)
            recordedVoiceNotes?.add(json.toString())
            SharedPreferencesManager.setRecordedVoiceNote(recordedVoiceNotes)
            updateListView()

            registerNewStopRecordEvent(location)
        }
    }

    private fun registerNewStopRecordEvent(location: String) {
        var event =
            EventRequest("NEW_VOICE_NOTE", "ACTIVO", "New voice note recorded near $location")
        registerEvent(event)
    }

    fun onRecordClick(view: View) {
        if(!checkPermissions(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ))){
            return
        }

        record()
    }

    private fun setupMediaRecorder() {
        if(!checkPermissions(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ))){
            return
        }

        outputDirectory =
            this.getExternalFilesDir(null)?.absolutePath + "/" + getString(R.string.app_name) + "/"
        File(outputDirectory).mkdir()
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
    }

    private fun record() {
        isRecording = true

        stopBtn.visibility = View.VISIBLE
        recordBtn.visibility = View.GONE
        pauseBtn.visibility = View.VISIBLE

        setupMediaRecorder()

        var currentDate = formatter.format(Date())
        audioFilePath = "$outputDirectory$currentDate.mp3"
        mediaRecorder.setOutputFile(audioFilePath)

        try {
            mediaRecorder.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "An error occurred while saving the voice note",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        mediaRecorder.start()

        opacityFilter.visibility = View.VISIBLE

        Glide.with(this)
            .load(R.drawable.recording)
            .into(imageView)

        registerNewStartRecordEvent()
    }

    private fun registerNewStartRecordEvent() {
        var event = EventRequest("RECORDING_NEW_VOICE_NOTE", "ACTIVO", "Recording new voice note")
        registerEvent(event)
    }

    fun onResumeClick(view: View) {
        mediaRecorder.resume()
        resumeBtn.visibility = View.GONE
        pauseBtn.visibility = View.VISIBLE
        isRecording = true
    }

    fun onPauseClick(view: View) {
        mediaRecorder.pause()
        pauseBtn.visibility = View.GONE
        resumeBtn.visibility = View.VISIBLE
        isRecording = false
    }

    private fun updateListView() {
        if(!checkPermissions(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ))){
            return
        }

        var list = mutableListOf<VoiceNote>()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes()
        recordedVoiceNotes?.forEach { s ->
            var v = gson.fromJson(s, VoiceNote::class.java)
            list.add(v)
        }

        val emptyText = findViewById<View>(android.R.id.empty) as TextView

        recyclerView = findViewById(R.id.voiceNotesList) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.RecyclerAdapter(list, this)
        recyclerView.adapter = adapter

        if (list.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
    }
}


