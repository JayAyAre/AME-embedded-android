package com.example.jordiramirezpedromendoza_ame_exercise3

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.util.*

class BluetoothActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var connectedSocket: BluetoothSocket? = null
    private val MODULE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val moduleName = "JordiRPedroM"
    private lateinit var connectionStatusTextView: TextView
    private lateinit var bluetoothTemperatureTextView: TextView
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        connectionStatusTextView = findViewById(R.id.connectionStatus)
        bluetoothTemperatureTextView = findViewById(R.id.bluetoothTemperature)

        val btnConnect: Button = findViewById(R.id.btnConnectBluetooth)
        btnConnect.setOnClickListener {
            checkAndRequestPermissions {
                connectToBluetoothModule()
            }
        }

        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }

    @SuppressLint("InlinedApi")
    private fun checkAndRequestPermissions(onGranted: () -> Unit) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            onGranted()
        } else {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        }
    }

    private fun connectToBluetoothModule() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Disconnected", "#FF0000")
            saveConnectionStatus(false)
            return
        }

        val module: BluetoothDevice? = pairedDevices.find { it.name == moduleName }
        if (module == null) {
            Toast.makeText(this, "Module '$moduleName' not found. Pair it first.", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Disconnected", "#FF0000")
            saveConnectionStatus(false)
            return
        }

        try {
            connectedSocket = module.createRfcommSocketToServiceRecord(MODULE_UUID)
            connectedSocket?.connect()
            Toast.makeText(this, "Connected to $moduleName", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Connected to $moduleName", "#008000")
            saveConnectionStatus(true)
            connectedSocket?.inputStream?.let { readDataFromModule(it) }
        } catch (e: IOException) {
            Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
            connectedSocket?.close()
            updateConnectionStatus("Disconnected", "#FF0000")
            saveConnectionStatus(false)
        }
    }

    private fun saveConnectionStatus(isConnected: Boolean) {
        val sharedPreferences = getSharedPreferences("BluetoothPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isConnected", isConnected)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun updateConnectionStatus(status: String, color: String) {
        runOnUiThread {
            connectionStatusTextView.text = "Connection Status: $status"
            connectionStatusTextView.setTextColor(android.graphics.Color.parseColor(color))
        }
    }

    private fun readDataFromModule(inputStream: InputStream) {
        val buffer = ByteArray(1024)
        var bytes: Int

        Thread {
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedData = String(buffer, 0, bytes)
                    val temperature = parseTemperature(receivedData)
                    if (temperature != null) {
                        saveTemperature(temperature)
                        updateTemperatureUI(temperature)
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this, "Disconnected: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }
        }.start()
    }

    private fun parseTemperature(data: String): Float? {
        return try {
            data.substringAfter("Temp: ").substringBefore("C").toFloat()
        } catch (e: Exception) {
            null
        }
    }

    private fun saveTemperature(temperature: Float) {
        val sharedPreferences = getSharedPreferences("BluetoothPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("temperature", temperature)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTemperatureUI(temperature: Float) {
        runOnUiThread {
            bluetoothTemperatureTextView.text = "Bluetooth Temperature: ${"%.2f".format(temperature)}°C"
        }
    }
}
