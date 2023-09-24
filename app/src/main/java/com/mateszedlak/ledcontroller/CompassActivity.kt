package com.mateszedlak.ledcontroller

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.net.URL
import kotlin.math.abs

class CompassActivity : Activity(), SensorEventListener {
  private val HOST = "http://192.168.50.125:3000"
  val url = URL("$HOST/v1/animation")

  private var lastUpdateTime = System.currentTimeMillis()
  private var lastBrightnessValue = -1 // Initialize with a value that's not achievable

  private val MIN_UPDATE_INTERVAL: Long = 100 // Minimum interval between updates in milliseconds

  private val handler: Handler = Handler(Looper.getMainLooper())

  private var sensorManager: SensorManager? = null
  private var compassSensor: Sensor? = null
  private var deviationTextView: TextView? = null
  val utilities = Utilities()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_compass)
    deviationTextView = findViewById(R.id.deviationTextView)
    sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    compassSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)
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
    val deviation = if (azimuth >= 180) (360 - azimuth) else azimuth
    val brightnessValue = ((180 - deviation) / 180 * 250).toInt()
    if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL &&
        abs(lastBrightnessValue - brightnessValue) > 1) {
      lastBrightnessValue = brightnessValue
      lastUpdateTime = currentTime
      // Calculate the deviation from North (0 degrees)

      handler.post {
        deviationTextView!!.text = "Deviation from North: $deviation degrees"
        utilities.sendHttpRequest(url, "{BRIGHTNESS:${brightnessValue}}")
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    // Not needed for this example
  }

  companion object {
    private const val TAG = "CompassActivity"
  }
}
