#include "mbed.h"
#include "ThermistorLib.h"
#include "BufferedSerial.h"
#include "ThisThread.h"

int main() {
    // Thermistor constants
    const float Rb = 100000.0; // 100 kΩ
    const float T0 = 298.15;   // Reference temperature in Kelvin
    const float K = 273.15;    // Conversion constant from Kelvin to Celsius
    const float beta = 4250.0; // Beta value of the thermistor

    // Create an instance of the thermistor
    Thermistor thermistor(A0, Rb, beta, T0, K); // Analog pin A0 for the thermistor

    // Bluetooth module configuration
    BufferedSerial blueToothSerial(D1, D0, 9600); // Bluetooth on pins D1 (TX) and D0 (RX)

    // Initial Bluetooth module configuration with AT commands
    blueToothSerial.write("AT", sizeof("AT"));
    ThisThread::sleep_for(400ms);

    blueToothSerial.write("AT+DEFAULT", sizeof("AT+DEFAULT"));
    ThisThread::sleep_for(2000ms);

    blueToothSerial.write("AT+NAMEJordiRPedroM", sizeof("AT+NAMEJordiRPedroM"));
    ThisThread::sleep_for(400ms);

    blueToothSerial.write("AT+ROLEM", sizeof("AT+ROLEM"));
    ThisThread::sleep_for(400ms);

    blueToothSerial.write("AT+AUTH1", sizeof("AT+AUTH1"));
    ThisThread::sleep_for(400ms);

    blueToothSerial.write("AT+CLEAR", sizeof("AT+CLEAR"));
    ThisThread::sleep_for(400ms);

    blueToothSerial.set_format(
        /* bits */ 8,
        /* parity */ BufferedSerial::None,
        /* stop bit */ 1
    );

    // Main loop
    while (true) {
        char buff[20]; // Buffer to store the data to send
        float temperature = thermistor.readTemperatureC(); // Temperature reading

        // Format the temperature data
        snprintf(buff, sizeof(buff), "Temp: %.2fC", temperature);

        // Send the data via Bluetooth
        blueToothSerial.write(buff, sizeof(buff));

        // Pause before the next iteration
        ThisThread::sleep_for(1500ms);
    }
}
