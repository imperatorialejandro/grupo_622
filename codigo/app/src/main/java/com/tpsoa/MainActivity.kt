package com.tpsoa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
    private val SHAKE_THRESHOLD: Float = 10.25f
    private val MIN_TIME_BETWEEN_SHAKES = 1000
    private var lastShakeTime: Long = 0

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private val formatter = SimpleDateFormat("dd-MM-yyyy HHmm")

    private var recordedVoiceNotes: MutableSet<String>? = null

    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLogged()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes(applicationContext)

        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no accelerometer sensor", Toast.LENGTH_SHORT).show()
        }

        updateListView()

        audioSetup()
    }

    private fun init() {
        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes(applicationContext)

        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no accelerometer sensor", Toast.LENGTH_SHORT).show()
        }

        updateListView()

        audioSetup()
    }

    private fun checkLogged() {
        if (!isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        var loggedUser = SharedPreferencesManager.getUserLogged(applicationContext)
        email_user_text.text = "Hello $loggedUser!"

//        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes(applicationContext)

//        init()
    }

    private fun isLogged(): Boolean {
        return SharedPreferencesManager.getUserLogged(applicationContext) != ""
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
        SharedPreferencesManager.clearPrefs(applicationContext)
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
                Log.i("asd", "isRecording: " + isRecording)
                if (isRecording) {
                    Log.i("asd", "deja de grabar")
                    stop()
                } else {
                    Log.i("asd", "pasa a grabar")
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
        sensorManager?.unregisterListener(this);
    }

    fun onStopClick(view: View) {
        stop()
    }

    private fun stop() {
        stopBtn.isEnabled = false
        stopBtn.visibility = View.INVISIBLE

        pauseResumeBtn.isEnabled = false
        pauseResumeBtn.visibility = View.INVISIBLE

        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        recordBtn.isEnabled = true
        recordBtn.visibility = View.VISIBLE

        Toast.makeText(this, "Saving " + audioFilePath, Toast.LENGTH_SHORT).show()

        var v = audioFilePath?.let { VoiceNote(it, "ramos Mejia") }
        var json = gson.toJson(v)
        recordedVoiceNotes?.add(json.toString())
        SharedPreferencesManager.setRecordedVoiceNote(applicationContext, recordedVoiceNotes)

        updateListView()
    }

    fun onRecordClick(view: View) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        } else {
            record()
        }
    }

    private fun record() {
        isRecording = true

        stopBtn.isEnabled = true
        stopBtn.visibility = View.VISIBLE

        recordBtn.isEnabled = false
        recordBtn.visibility = View.INVISIBLE

        pauseResumeBtn.isEnabled = true
        pauseResumeBtn.visibility = View.VISIBLE
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(
                MediaRecorder.OutputFormat.DEFAULT
            )

            var currentDate = formatter.format(Date())

            File(Environment.getExternalStorageDirectory().absolutePath + "/" + getString(R.string.app_name)).mkdir()

            audioFilePath =
                Environment.getExternalStorageDirectory().absolutePath + "/" + getString(R.string.app_name) + "/voice note " + currentDate + ".mp3"
            mediaRecorder?.setOutputFile(audioFilePath)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            mediaRecorder?.prepare()

            mediaRecorder?.start()
            Toast.makeText(this, "Recording " + audioFilePath, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE
        )
    }

    private fun audioSetup() {
        if (!hasMicrophone()) {
            stopBtn.isEnabled = false
            recordBtn.isEnabled = false
        } else {
            stopBtn.isEnabled = false
            pauseResumeBtn.isEnabled = false
        }
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
        mediaRecorder?.resume()
        pauseResumeBtn.text = "Pause"
        isRecording = true
    }

    private fun pauseRecording() {
        Toast.makeText(this, "Voice note paused", Toast.LENGTH_SHORT).show()
        mediaRecorder?.pause()
        isRecording = false
        pauseResumeBtn.text = "Resume"
    }

    private fun updateListView() {
        var list = mutableListOf<VoiceNote>()

        recordedVoiceNotes = SharedPreferencesManager.getRecordedVoiceNotes(applicationContext)
        recordedVoiceNotes?.forEach { s ->
            Log.i("asd", s)
            var v = gson.fromJson<VoiceNote>(s, VoiceNote::class.java)
            list.add(v)
        }

        listView.adapter = ListAdapter(this, R.layout.item, list)
    }
}
