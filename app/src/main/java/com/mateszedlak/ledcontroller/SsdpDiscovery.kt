package com.mateszedlak.ledcontroller

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

private const val TAG = "SsdpDiscoverer"

class SsdpDiscoverer(private val context: Context, private val listener: Listener) {
  private lateinit var nsdManager: NsdManager
  private lateinit var discoveryListener: NsdManager.DiscoveryListener

  interface Listener {
    fun onDeviceFound(deviceInfo: NsdServiceInfo)
    fun onDiscoveryError(errorMessage: String)
  }

  fun startDiscovery() {
    nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    discoveryListener =
        object : NsdManager.DiscoveryListener {
          override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG, "SSDP discovery started")
          }

          override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            if (serviceInfo.serviceType == "_http._tcp.") {
              listener.onDeviceFound(serviceInfo)
            }
          }

          override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "SSDP discovery stopped")
          }

          override fun onServiceLost(serviceInfo: NsdServiceInfo) {}

          override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            listener.onDiscoveryError("SSDP discovery start failed with error code: $errorCode")
          }

          override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            listener.onDiscoveryError("SSDP discovery stop failed with error code: $errorCode")
          }
        }

    nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
  }

  fun stopDiscovery() {
    nsdManager.stopServiceDiscovery(discoveryListener)
  }
}
