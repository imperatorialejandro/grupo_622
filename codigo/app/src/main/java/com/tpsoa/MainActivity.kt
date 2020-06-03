package com.tpsoa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tpsoa.common.GpsUtils
import com.google.gson.Gson
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
    private var isRecording = false

    private val formatter = SimpleDateFormat("dd-MM-yyyy HHmm")

    private var recordedVoiceNotes: MutableSet<String>? = null

    private var gson = Gson()

    lateinit var mRecyclerView : RecyclerView
    val mAdapter : RecyclerAdapter = RecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLogged()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes()

        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no accelerometer sensor", Toast.LENGTH_SHORT).show()
        }

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
        SharedPreferencesManager.clearPrefs()
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
        stopBtn.visibility = View.INVISIBLE

        pauseResumeBtn.visibility = View.INVISIBLE

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
        }
    }

    fun onRecordClick(view: View) {
        if (!hasRecordPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10)
            return
        }
        record()
    }

    private fun hasRecordPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
    private fun record() {
        isRecording = true

        stopBtn.visibility = View.VISIBLE

        recordBtn.visibility = View.INVISIBLE

        pauseResumeBtn.visibility = View.VISIBLE

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)

        File(Environment.getExternalStorageDirectory().absolutePath + "/" + getString(R.string.app_name)).mkdir()

        var currentDate = formatter.format(Date())
        audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/" + getString(R.string.app_name) + "/voice note " + currentDate + ".mp3"
        mediaRecorder.setOutputFile(audioFilePath)

        try {
            mediaRecorder.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder.start()
        Toast.makeText(this, "Recording " + audioFilePath, Toast.LENGTH_SHORT).show()
    }

    fun onPauseResumeClick(view: View) {
        if (isRecording) {
            pauseRecording()
        } else {
            resumeRecording()
        }
    }

    private fun resumeRecording() {
        Toast.makeText(this, "Resuming voice note", Toast.LENGTH_SHORT).show()
        mediaRecorder.resume()
        isRecording = true
    }

    private fun pauseRecording() {
        Toast.makeText(this, "Voice note paused", Toast.LENGTH_SHORT).show()
        mediaRecorder.pause()
        mediaRecorder.pause()
        isRecording = false
    }

    private fun updateListView() {
        var list = mutableListOf<VoiceNote>()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes()
        recordedVoiceNotes?.forEach { s ->
            var v = gson.fromJson(s, VoiceNote::class.java)
            list.add(v)
        }

        val emptyText = findViewById<View>(android.R.id.empty) as TextView

        mRecyclerView = findViewById(R.id.voiceNotesList) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.RecyclerAdapter(list, this)
        mRecyclerView.adapter = mAdapter

        if(list.isEmpty()){
            mRecyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        }
        else {
            mRecyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
    }
}


