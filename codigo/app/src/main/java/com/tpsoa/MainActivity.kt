package com.tpsoa

import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    //private var lightSensor: Sensor? = null
    private var accelerometerSensor : Sensor ?= null
    //acelerometro
    val SHAKE_THRESHOLD: Float = 10.25f // How hard should user shake to invoke the service
    val MIN_TIME_BETWEEN_SHAKES = 1000
    private var lastShakeTime: Long = 0
    //audio recorder
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private val RECORD_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102

    private val formatter = SimpleDateFormat("dd-MM-yyyy hhmm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogged()
    }

    private fun init(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        audioSetup()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this);
    }

    private fun checkLogged() {
        if (!SharedPreferencesManager.isLogged(applicationContext)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)
            init()
        }
    }

    fun onLogoutClick(view: View) {
        SharedPreferencesManager.clear(applicationContext)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()
        if (curTime - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt(
                Math.pow(x.toDouble(), 2.0) +
                        Math.pow(y.toDouble(), 2.0) +
                        Math.pow(z.toDouble(), 2.0)
            ) - SensorManager.GRAVITY_EARTH
            if (acceleration > SHAKE_THRESHOLD) {
                lastShakeTime = curTime
//                if (!isFlashlightOn) {
//                    torchToggle("on")
//                    isFlashlightOn = true
//                } else {
//                    torchToggle("off")
//                    isFlashlightOn = false
//                }
                Log.i("asd","shake")
                Log.i("asd","isRecording: "+isRecording)
                if(isRecording){
                    Log.i("asd","deja de grabar")
                    stop()
                }
                else {
                    Log.i("asd","pasa a grabar")
                    record()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, this.accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, this.lightSensor, SensorManager.SENSOR_DELAY_NORMAL,handler);

    }

    fun onStopClick(view: View) {
        stop()
    }

    private fun stop(){
        stopBtn.isEnabled = false
        pauseResumeBtn.isEnabled = true

        if (isRecording) {
//            recordBtn.isEnabled = false
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
        record()
    }

    fun record(){
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

    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE)
    }

    private fun audioSetup() {
        if (!hasMicrophone()) {
            stopBtn.isEnabled = false
            recordBtn.isEnabled = false
        } else {
            stopBtn.isEnabled = false
            pauseResumeBtn.isEnabled = false
        }

//        audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/voice note.mp3"
//
//        Log.i("asd",audioFilePath)
    }

    fun onPauseResumeClick(view: View) {
        if(isRecording)
            pauseRecording()
        else
            resumeRecording()
    }

    private fun resumeRecording() {
        Toast.makeText(this,"Resuming voice note", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        pauseResumeBtn.text = "Pause"
//        recordingStopped = false
        isRecording = true
    }

    private fun pauseRecording() {
        Toast.makeText(this,"Voice note paused", Toast.LENGTH_SHORT).show()
        mediaRecorder?.pause()
        isRecording = false
        pauseResumeBtn.text = "Resume"
    }
}
