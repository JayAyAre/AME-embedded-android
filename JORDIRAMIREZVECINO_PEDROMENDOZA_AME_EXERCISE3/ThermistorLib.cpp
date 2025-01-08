// ThermistorLib.cpp

#include "ThermistorLib.h"
#include <cmath>

Thermistor::Thermistor(PinName pin, float Rb, float beta, float T0, float K)
    : therm(pin), Rb(Rb), beta(beta), T0(T0), K(K) {}


float Thermistor::readTemperatureC() {
    float lecture = therm.read();
    float rtherm = Rb * ((1 / lecture) - 1);
    float temperatureK = 1.0 / ((log(rtherm / Rb) / beta) + (1.0 / T0));
    return temperatureK - K;
}


// Function to read the voltage
float Thermistor::readVoltage() {
    return therm.read();
}
