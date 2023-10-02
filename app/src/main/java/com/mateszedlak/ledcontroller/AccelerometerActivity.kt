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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerometerActivity : Activity(), SensorEventListener {
  private val HOST = "ws://esp8266.local:81/"

  private var lastUpdateTime = System.currentTimeMillis()
  private var lastBrightnessValue = -1 // Initialize with a value that's not achievable

  private val MIN_UPDATE_INTERVAL: Long = 0 // Minimum interval between updates in milliseconds

  private val handler: Handler = Handler(Looper.getMainLooper())

  private var sensorManager: SensorManager? = null
  private var accelerometerSensor: Sensor? = null
  private var accelerationTextView: TextView? = null

  private lateinit var webSocketListener: WebSocketListener
  private val okHttpClient = OkHttpClient()
  private var webSocket: WebSocket? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_accelerometer)
    webSocketListener = WebSocketListener()
    webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
    accelerationTextView = findViewById(R.id.accelerationTextView)
    sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
  }

  override fun onResume() {
    super.onResume()
    sensorManager!!.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
  }

  override fun onPause() {
    super.onPause()
    sensorManager!!.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent) {
    val SENSOR_SENSITIVITY = 0.1f
    val currentTime = System.currentTimeMillis()
    val accelerationForceX = event.values[0]
    val accelerationForceY = event.values[1]
    val accelerationForceZ = event.values[2]
    val accelerationValue =
        sqrt(
            accelerationForceX.pow(2.0f) +
                accelerationForceY.pow(2.0f) +
                accelerationForceZ.pow(2.0f))

    if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL) {
      lastUpdateTime = currentTime
      Log.d(TAG, "X: $accelerationForceX  Y: $accelerationForceY    Z: $accelerationForceZ")
      Log.d(TAG, "accelerationValue: $accelerationValue")

      val absoluteAccelerationX = abs(accelerationForceX)
      val absoluteAccelerationY = abs(accelerationForceY)
      val absoluteAccelerationZ = abs(accelerationForceZ)

      val brightnessValue = max((accelerationValue * 50f).toInt(), 30)
      val appliedBrightness = min(if (brightnessValue < 20) 0 else brightnessValue, 250)
      Log.d(TAG, "Brightness: $brightnessValue")
      Log.d(TAG, "appliedBrightness: $appliedBrightness")

      if (lastBrightnessValue !== appliedBrightness) {
        lastBrightnessValue = appliedBrightness
        handler.post {
          accelerationTextView!!.text =
              "X: $accelerationForceX    Y: $accelerationForceY    Z:$accelerationForceZ"

          webSocket?.send("{BRIGHTNESS:$appliedBrightness}")
          // utilities.sendHttpRequest(url, "{BRIGHTNESS:${appliedBrightness}}")
        }
      }
    }
  }

  private fun createRequest(): Request {
    val websocketURL = HOST

    return Request.Builder().url(websocketURL).build()
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    // Not needed for this example
  }

  companion object {
    private const val TAG = "CompassActivity"
  }
}
