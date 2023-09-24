package com.mateszedlak.ledcontroller

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class Utilities {

  @OptIn(DelicateCoroutinesApi::class)
  open fun sendHttpRequest(url: URL, data: String) {
    GlobalScope.launch(Dispatchers.IO) {
      var responseCode = -1
      try {
        // Log.d(TAG, data)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Connection", "Keep-Alive") // Set Keep-Alive header
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

      withContext(Dispatchers.Main) {
        // Log.d(TAG, "Response code: $responseCode")
      }
    }
  }

  companion object {
    private const val TAG = "Utilities"
  }
}
