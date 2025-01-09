package com.example.jordiramirezpedromendoza_ame_exercise3

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TemperatureActivity : AppCompatActivity() {

    private lateinit var currentCityTextView: TextView
    private lateinit var currentWeatherTextView: TextView
    private lateinit var forecastTextView: TextView
    private lateinit var bluetoothTemperatureTextView: TextView
    private lateinit var searchButton: Button
    private lateinit var citySearchEditText: EditText

    private val openWeatherApiKey = "11f73830671436f70048dc5319785a7a"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature)

        currentCityTextView = findViewById(R.id.currentCity)
        currentWeatherTextView = findViewById(R.id.currentWeather)
        forecastTextView = findViewById(R.id.forecast)
        bluetoothTemperatureTextView = findViewById(R.id.bluetoothTemperature)
        searchButton = findViewById(R.id.searchButton)
        citySearchEditText = findViewById(R.id.citySearchEditText)

        displayBluetoothTemperature()

        searchButton.setOnClickListener {
            val cityName = citySearchEditText.text.toString()
            if (cityName.isNotEmpty()) {
                fetchWeatherForCity(cityName)
                fetchWeatherForecast(cityName)
            } else {
                Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayBluetoothTemperature() {
        val sharedPreferences = getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE)
        val bluetoothTemperature = sharedPreferences.getFloat("temperature", -1f)
        if (bluetoothTemperature != -1f) {
            bluetoothTemperatureTextView.text = "Bluetooth Temperature: ${"%.2f".format(bluetoothTemperature)}°C"
        } else {
            bluetoothTemperatureTextView.text = "Bluetooth Temperature: N/A"
        }
    }

    private fun fetchWeatherForCity(cityName: String) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$openWeatherApiKey&units=metric"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TemperatureActivity, "Failed to fetch weather", Toast.LENGTH_SHORT).show()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@TemperatureActivity, "Invalid city name", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val city = json.getString("name")
                    val temperature = json.getJSONObject("main").getDouble("temp")
                    val weather = json.getJSONArray("weather").getJSONObject(0).getString("description")

                    runOnUiThread {
                        currentCityTextView.text = "City: $city"
                        currentWeatherTextView.text = "Current Weather: $temperature°C, $weather"
                    }
                }
            }
        })
    }

    private fun fetchWeatherForecast(cityName: String) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$openWeatherApiKey&units=metric"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    forecastTextView.text = "Failed to fetch forecast"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@TemperatureActivity, "Invalid city name", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val list = json.getJSONArray("list")
                    val forecastText = StringBuilder()

                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val seenDates = mutableSetOf<String>()

                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)
                        val timestamp = item.getLong("dt") * 1000
                        val date = Date(timestamp)

                        val formattedDate = dateFormatter.format(date)

                        if (!seenDates.contains(formattedDate)) {
                            seenDates.add(formattedDate)

                            val temp = item.getJSONObject("main").getDouble("temp")
                            val weather = item.getJSONArray("weather").getJSONObject(0).getString("description")

                            forecastText.append("$formattedDate: $temp°C, $weather\n")
                        }

                        if (seenDates.size == 5) break
                    }

                    runOnUiThread {
                        forecastTextView.text = forecastText.toString()
                    }
                }
            }
        })
    }
}
