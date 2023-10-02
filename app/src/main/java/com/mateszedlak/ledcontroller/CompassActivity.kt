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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import kotlin.math.abs

class CompassActivity : Activity(), SensorEventListener {
  // private val HOST = "ws://esp8266.local:81/"
  private val HOST = "ws://192.168.50.165:81/"

  private var lastUpdateTime = System.currentTimeMillis()
  private var lastBrightnessValue = -1 // Initialize with a value that's not achievable

  private val MIN_UPDATE_INTERVAL: Long = 0 // Minimum interval between updates in milliseconds

  private val handler: Handler = Handler(Looper.getMainLooper())

  private var sensorManager: SensorManager? = null
  private var compassSensor: Sensor? = null
  private var deviationTextView: TextView? = null

  private lateinit var webSocketListener: WebSocketListener
  private val okHttpClient = OkHttpClient()
  private var webSocket: WebSocket? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_compass)
    deviationTextView = findViewById(R.id.deviationTextView)
    sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    compassSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    webSocketListener = WebSocketListener()
    val request = Request.Builder().url(HOST).build()
    webSocket = okHttpClient.newWebSocket(request, webSocketListener)
  }

  override fun onResume() {
    super.onResume()
    sensorManager!!.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_GAME)
  }

  override fun onPause() {
    super.onPause()
    sensorManager!!.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent) {
    val currentTime = System.currentTimeMillis()
    val azimuth = event.values[0]
    // Calculate the deviation from North (0 degrees)
    val deviation = if (azimuth >= 180) (360 - azimuth) else azimuth
    val brightnessValue = ((180 - deviation) / 180 * 250).toInt()
    if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL &&
        abs(lastBrightnessValue - brightnessValue) > 1) {
      lastBrightnessValue = brightnessValue
      lastUpdateTime = currentTime

      deviationTextView!!.text = "Deviation from North: $deviation degrees"
      webSocket?.send("{BRIGHTNESS:$brightnessValue}")
      Log.d(TAG, "onSensorChanged: $deviation")
    }
  }

  override fun onDestroy() {
    webSocket?.close(1000, "Activity destroyed")
    webSocket = null
    super.onDestroy()
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    // Not needed for this example
  }

  companion object {
    private const val TAG = "CompassActivity"
  }
}
