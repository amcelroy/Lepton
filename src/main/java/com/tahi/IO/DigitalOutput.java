package com.tahi.IO;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.RaspiPin;

public class DigitalOutput {
    public int Pin;
    public int ID;
    public String Name;

    GpioPinDigitalOutput gpioPin;
    
    public DigitalOutput(GpioController gpio, int id, int pin)
    {
        ID = id;
        this.Pin = pin;

        gpioPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pin), PinState.LOW);
        gpioPin.setShutdownOptions(true, PinState.LOW);
        gpioPin.setPullResistance(PinPullResistance.PULL_DOWN);
    }

    public void on()
    {
        gpioPin.high();
    }

    public void off()
    {
    	gpioPin.low();
    }
}
