package com.mateszedlak.ledcontroller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mateszedlak.ledcontroller.databinding.FragmentFirstBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** A simple [Fragment] subclass as the default destination in the navigation. */
class FirstFragment : Fragment() {

  private var _binding: FragmentFirstBinding? = null
  private val HOST = "http://192.168.50.18:3000"
  val url = URL("$HOST/blink")

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    _binding = FragmentFirstBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonOn.setOnClickListener {
      Log.i(TAG, "Button ON clicked")
      try {
        sendRequest("true")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }

    binding.buttonOff.setOnClickListener {
      Log.i(TAG, "Button OFF clicked")
      try {
        sendRequest("false")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  private fun sendRequest(data: String) {
    GlobalScope.launch(Dispatchers.IO) {
      var responseCode = -1
      try {
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

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val TAG = "FirstFragment"
  }
}
