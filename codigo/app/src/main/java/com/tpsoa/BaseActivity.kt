package com.tpsoa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tpsoa.model.EventRequest
import com.tpsoa.model.EventResponse
import com.tpsoa.rest.ApiInterface
import com.tpsoa.rest.ServiceBuilder
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


open class BaseActivity : AppCompatActivity(), SensorEventListener {

    var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null

    var isRecording: Boolean = false

    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    val MULTIPLE_PERMISSIONS_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            Toast.makeText(this, "Device has no light sensor", Toast.LENGTH_SHORT).show()
        }
    }

    open fun init(){}

    fun checkPermissions(permissions: Array<String>): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(applicationContext, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        when (requestCode) {
            MULTIPLE_PERMISSIONS_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                } else {
                    var perStr = ""
                    for (per in permissions) {
                        perStr += """
                            
                            $per
                            """.trimIndent()
                    }
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_LIGHT) {
            if (isRecording) {
                return
            }

            val value: Float = event.values[0]
            val mode: Int = AppCompatDelegate.getDefaultNightMode()
            if (value < 100 && mode != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else if (value > 500 && mode != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerSensorTriggered(event)
        }
    }

    open fun accelerometerSensorTriggered(event: SensorEvent) {}

    fun registerEvent(event: EventRequest) {
        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val call = request.RegisterEvent(event)

        call.enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                if (response.isSuccessful) {
                    val res = response.body() as EventResponse
                    Log.i("Event registered", res.event.type)
                } else {
                    var errorBody = JSONObject(response.errorBody()!!.string())
                    Log.e("Error registering event", errorBody.get("msg").toString())
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                Log.e("Error registering event", t.message)
            }
        })
    }
}
