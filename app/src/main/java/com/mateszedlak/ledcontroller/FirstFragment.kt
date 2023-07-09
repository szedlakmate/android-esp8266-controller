package com.mateszedlak.ledcontroller

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mateszedlak.ledcontroller.databinding.FragmentFirstBinding
import java.net.URL

/** A simple [Fragment] subclass as the default destination in the navigation. */
class FirstFragment : Fragment(), SsdpDiscoverer.Listener {

  private var _binding: FragmentFirstBinding? = null
  private val HOST = "http://192.168.50.18:3000"
  val url = URL("$HOST/blink")
  private lateinit var ssdpDiscoverer: SsdpDiscoverer

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

    ssdpDiscoverer = SsdpDiscoverer(requireContext(), this)
    ssdpDiscoverer.startDiscovery()

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonOn.setOnClickListener {
      Log.i(TAG, "Button ON clicked")
      try {
        sendHttpRequest(url.toString(), "true")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }

    binding.buttonOff.setOnClickListener {
      Log.i(TAG, "Button OFF clicked")
      try {
        sendHttpRequest(url.toString(), "false")
      } catch (e: Exception) {
        Log.w(TAG, "Error: ${e.message}")
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    ssdpDiscoverer.stopDiscovery()
    _binding = null
  }

  override fun onDeviceFound(deviceInfo: NsdServiceInfo) {
    // Process the discovered device
    Log.d(TAG, "Device found: ${deviceInfo.serviceName}, IP: ${deviceInfo.host.hostAddress}")
  }

  override fun onDiscoveryError(errorMessage: String) {
    // Handle discovery error
    Log.e(TAG, "Discovery error: $errorMessage")
  }

  companion object {
    private const val TAG = "FirstFragment"
  }
}
