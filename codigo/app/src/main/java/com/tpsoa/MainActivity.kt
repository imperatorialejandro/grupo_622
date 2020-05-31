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
import com.tpsoa.common.GpsUtils
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class MainActivity : BaseActivity() {

    private var accelerometerSensor : Sensor ?= null
    private val SHAKE_THRESHOLD: Float = 10.25f
    private val MIN_TIME_BETWEEN_SHAKES = 1000
    private var lastShakeTime: Long = 0

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    private val formatter = SimpleDateFormat("dd-MM-yyyy hhmm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLogged()

        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(this, "Device has no accelerometer sensor", Toast.LENGTH_SHORT).show()
        }

        GpsUtils.getLocation(this) { location ->
            Log.d("got_location", location)
        }

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
                Log.i("asd","isRecording: "+isRecording)
                if(isRecording){
                    stop()
                }
                else {
                    record()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, this.accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this);
    }

    fun onStopClick(view: View) {
        stop()
    }

    private fun stop(){
        stopBtn.isEnabled = false
        pauseResumeBtn.isEnabled = true

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
        Toast.makeText(this,"Saving "+audioFilePath, Toast.LENGTH_SHORT).show()
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

    private fun record(){
        isRecording = true
        stopBtn.isEnabled = true
        recordBtn.isEnabled = false
        pauseResumeBtn.isEnabled = true
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(
                MediaRecorder.OutputFormat.DEFAULT)

            var currentDate = formatter.format(Date())

            audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/voice note "+currentDate+".mp3"
            mediaRecorder?.setOutputFile(audioFilePath)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            mediaRecorder?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder?.start()
        Toast.makeText(this,"Recording "+audioFilePath, Toast.LENGTH_SHORT).show()
    }

    fun onPauseResumeClick(view: View) {
        if(isRecording) {
            pauseRecording()
        }
        else {
            resumeRecording()
        }
    }

    private fun resumeRecording() {
        Toast.makeText(this,"Resuming voice note", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        pauseResumeBtn.text = "Pause"
        isRecording = true
    }

    private fun pauseRecording() {
        Toast.makeText(this,"Voice note paused", Toast.LENGTH_SHORT).show()
        mediaRecorder?.pause()
        isRecording = false
        pauseResumeBtn.text = "Resume"
    }

}


