package com.example.jordiramirezpedromendoza_ame_exercise3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar el mensaje de bienvenida
        TextView welcomeMessage = findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("¡Bienvenido a la aplicación!");

        // Configurar los botones y su lógica de navegación
        Button btnBluetooth = findViewById(R.id.btnConnectBluetooth);
        Button btnTemperature = findViewById(R.id.btnViewTemperature);

        // Inicialmente, deshabilitar el botón de temperaturas
        btnTemperature.setEnabled(false);

        // Configurar acción del botón para ir a la actividad de Bluetooth
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        // Configurar acción del botón para ir a la actividad de temperaturas
        btnTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Verificar el estado de la conexión en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPrefs", MODE_PRIVATE);
        boolean isConnected = sharedPreferences.getBoolean("isConnected", false);

        // Habilitar o deshabilitar el botón de "Ver Temperaturas"
        Button btnTemperature = findViewById(R.id.btnViewTemperature);
        btnTemperature.setEnabled(isConnected); // Habilitar solo si está conectado
    }

}
