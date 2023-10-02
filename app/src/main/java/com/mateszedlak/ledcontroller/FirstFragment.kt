package com.mateszedlak.ledcontroller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mateszedlak.ledcontroller.databinding.FragmentFirstBinding
import java.net.URL

/** A simple [Fragment] subclass as the default destination in the navigation. */
class FirstFragment : Fragment() {

  private var _binding: FragmentFirstBinding? = null
  private val HOST = "http://192.168.50.125:3000"
  val url = URL("$HOST/v1/animation")

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
        //  sendHttpRequest(url, "{REVERSED:1,WAVE_LENGTH_SCALE:0.333}")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }

    binding.buttonOff.setOnClickListener {
      Log.i(TAG, "Button OFF clicked")
      try {
        //  sendHttpRequest(url, "{REVERSED:0,WAVE_LENGTH_SCALE:2.0}")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }

    binding.buttonCompass.setOnClickListener { openCompassActivity() }
    binding.buttonAccelerometer.setOnClickListener { openAccelerometerActivity() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  fun openCompassActivity() {
    val intent = Intent(this.context, CompassActivity::class.java)
    startActivity(intent)
  }

  fun openAccelerometerActivity() {
    val intent = Intent(this.context, AccelerometerActivity::class.java)
    startActivity(intent)
  }

  companion object {
    private const val TAG = "FirstFragment"
  }
}
