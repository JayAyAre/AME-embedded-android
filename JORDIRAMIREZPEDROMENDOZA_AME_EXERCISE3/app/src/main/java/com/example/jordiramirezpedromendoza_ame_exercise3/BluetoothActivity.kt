package com.example.jordiramirezpedromendoza_ame_exercise3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.util.*

class BluetoothActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var connectedSocket: BluetoothSocket? = null
    private val MODULE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID estándar SPP
    private val moduleName = "JordiRPedroM" // Nombre del módulo configurado en la placa

    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var connectionStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        // Inicializar el TextView para mostrar el estado de la conexión
        connectionStatusTextView = findViewById(R.id.connectionStatus)

        // Botón para conectar al módulo
        val btnConnect: Button = findViewById(R.id.btnConnectBluetooth)
        btnConnect.setOnClickListener {
            // Verificar permisos y conectar
            checkAndRequestPermissions()
            connectToBluetoothModule()
        }

        // Inicializar BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Si el Bluetooth está apagado, solicitar al usuario que lo encienda
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            startActivityForResult(enableBtIntent, 1)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun connectToBluetoothModule() {
        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener dispositivos emparejados
        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Desconectado", "#FF0000")
            saveConnectionStatus(false) // Guardar que NO estamos conectados
            return
        }

        // Buscar el módulo emparejado por nombre
        val module: BluetoothDevice? = pairedDevices.find { it.name == moduleName }
        if (module == null) {
            Toast.makeText(this, "Module '$moduleName' not found. Pair it first.", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Desconectado", "#FF0000")
            saveConnectionStatus(false) // Guardar que NO estamos conectados
            return
        }

        // Intentar conectar al módulo
        try {
            connectedSocket = module.createRfcommSocketToServiceRecord(MODULE_UUID)
            connectedSocket?.connect()
            Toast.makeText(this, "Connected to $moduleName", Toast.LENGTH_SHORT).show()
            updateConnectionStatus("Conectado a $moduleName", "#008000")

            // Guardar el estado de conexión en SharedPreferences como exitoso
            saveConnectionStatus(true)

            // Leer datos del módulo si es necesario
            connectedSocket?.inputStream?.let { readDataFromModule(it) }
        } catch (e: IOException) {
            Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
            connectedSocket?.close()
            updateConnectionStatus("Desconectado", "#FF0000")
            saveConnectionStatus(false) // Guardar que NO estamos conectados
        }
    }

    // Método para guardar el estado de la conexión en SharedPreferences
    private fun saveConnectionStatus(isConnected: Boolean) {
        val sharedPreferences = getSharedPreferences("BluetoothPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isConnected", isConnected)
        editor.apply()
    }



    private fun updateConnectionStatus(status: String, color: String) {
        runOnUiThread {
            connectionStatusTextView.text = "Estado de conexión: $status"
            connectionStatusTextView.setTextColor(android.graphics.Color.parseColor(color))
        }
    }

    private fun readDataFromModule(inputStream: InputStream) {
        val buffer = ByteArray(1024)
        var bytes: Int

        // Hilo para leer datos continuamente
        Thread {
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedData = String(buffer, 0, bytes)

                    runOnUiThread {
                        Toast.makeText(this, "Data: $receivedData", Toast.LENGTH_SHORT).show()
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
}
