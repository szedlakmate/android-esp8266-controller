package com.mateszedlak.ledcontroller

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "MainActivity"
private const val SERVICE_TYPE = "_http._tcp."

@OptIn(DelicateCoroutinesApi::class)
fun sendHttpRequest(urlString: String, data: String) {
  Log.d(TAG, "Sending HTTP request to $urlString with data: $data")
  GlobalScope.launch(Dispatchers.IO) {
    try {
      val url = URL(urlString)
      val connection = url.openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "text/plain")
      connection.doOutput = true

      connection.outputStream.bufferedWriter().use { writer -> writer.write(data) }

      val responseCode = connection.responseCode
      Log.i(TAG, "Response code: $responseCode")
    } catch (e: IOException) {
      Log.e(TAG, "Error: ${e.message}")
      e.printStackTrace()
    }
  }
}
