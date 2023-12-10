package com.example.BLEsample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.BLEsample.databinding.ActivityMainBinding


private const val RUNTIME_PERMISSION_REQUEST_CODE = 2
private lateinit var btLauncher: ActivityResultLauncher<Intent>
val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                }

                else -> {
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        registerBtLauncher()

         if (!mBluetoothAdapter.isEnabled) {
              binding.textView.text = "BLUETOOTH OFF"
           } else {
              binding.textView.text = "BLUETOOTH ON"
           }

        binding.apply {
            button.setOnClickListener(onClickListener())
            button2.setOnClickListener(onClickListener())
        }
    }


    @SuppressLint("SetTextI18n")
    private fun onClickListener(): View.OnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.button -> {

                if (!mBluetoothAdapter.isEnabled) {
                    btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }

            R.id.button2 -> {
                if (!mBluetoothAdapter.isEnabled) {
                    val text = "BLUETOOTH OFF"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                } else {

                    if (isScanning ) {
                        stopBleScan()
                    }
                    else {
                        startBleScan()
                    }
                }
            }
        }
    }


    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("SetTextI18n", "MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                binding.textView2.text = "Device Name: ${name ?: "Unnamed\n"}, address: $address\n"
                Log.i(
                    "ScanCallback",
                    "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                )
            }
        }
    }

    private fun startBleScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                    runOnUiThread {
                        Log.i("ScanCallback", "FIRST")
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            RUNTIME_PERMISSION_REQUEST_CODE
                        )
                    }
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    runOnUiThread {
                        Log.i("ScanCallback", "SECOND")
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ),
                            RUNTIME_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
        }

        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        isScanning = true
    }


    private var isScanning = false
        set(value) {
            field = value
                     runOnUiThread { binding.button2.text = if (value) "Stop Scan" else "Start Scan" }
        }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        isScanning = false
    }

    @SuppressLint("SetTextI18n")
    private fun registerBtLauncher() {
        btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.textView.text = "BLUETOOTH ON"
            }
        }
    }
}