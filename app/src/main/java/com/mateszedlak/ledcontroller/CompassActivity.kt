package com.mateszedlak.ledcontroller

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class CompassActivity : Activity(), SensorEventListener {
  private val HOST = "http://192.168.50.125:3000"
  val url = URL("$HOST/v1/animation")

  private var lastUpdateTime = System.currentTimeMillis()
  private var lastBrightnessValue = -1 // Initialize with a value that's not achievable

  private val MIN_UPDATE_INTERVAL: Long = 200 // Minimum interval between updates in milliseconds

  private val handler: Handler = Handler(Looper.getMainLooper())

  private var sensorManager: SensorManager? = null
  private var compassSensor: Sensor? = null
  private var deviationTextView: TextView? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_compass)
    deviationTextView = findViewById(R.id.deviationTextView)
    sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    compassSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)
  }

  override fun onResume() {
    super.onResume()
    sensorManager!!.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL)
  }

  override fun onPause() {
    super.onPause()
    sensorManager!!.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent) {
    val currentTime = System.currentTimeMillis()
    val azimuth = event.values[0]
    val deviation = if (azimuth >= 180) (360 - azimuth) else azimuth
    val brightnessValue = ((180 - deviation) / 180 * 250).toInt()
    if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL &&
        abs(lastBrightnessValue - brightnessValue) > 1) {
      lastBrightnessValue = brightnessValue
      lastUpdateTime = currentTime
      // Calculate the deviation from North (0 degrees)

      handler.post {
        deviationTextView!!.text = "Deviation from North: $deviation degrees"
        sendHttpRequest(url, "{BRIGHTNESS:${brightnessValue}}")
      }
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  fun sendHttpRequest(url: URL, data: String) {
    GlobalScope.launch(Dispatchers.IO) {
      var responseCode = -1
      try {
        Log.d(TAG, data)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.doOutput = true

        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.writeBytes(data)
        outputStream.flush()
        outputStream.close()

        responseCode = connection.responseCode
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
        e.printStackTrace()
      }

      withContext(Dispatchers.Main) { Log.d(TAG, "Response code: $responseCode") }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    // Not needed for this example
  }

  companion object {
    private const val TAG = "CompassActivity"
  }
}
