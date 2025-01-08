#ifndef THERMISTORLIB_H
#define THERMISTORLIB_H

#include "mbed.h"

class Thermistor {
public:
    // Constructor
    Thermistor(PinName pin, float Rb, float beta, float T0, float K);

    // Function to read the temperature in degrees Celsius
    float readTemperatureC();

    // Function to read the voltage
    float readVoltage();

private:
    AnalogIn therm;  // Analog input pin for the thermistor
    float Rb;        // Balancing resistance
    float beta;      // Beta value
    float T0;        // Reference temperature in Kelvin
    float K;         // Conversion constant to Celsius
};

#endif // THERMISTORLIB_H
