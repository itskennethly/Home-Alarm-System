package eecs;


import org.firmata4j.I2CDevice;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import java.io.IOException;
import java.util.Scanner;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import java.util.ArrayList;


public class HomeAlarm {
   private static IODevice arduino;
   private static int buzzerCount = 0;
   private static boolean alarmActive = false; // Alarm starts off
   private static boolean running = true; // Controls main loop
   private static final int MOTION_PIN = 15;  // A1 in Firmata4J (A1 = 15)
   private static final int SOUND_PIN = 5;
   private static final int LIGHT_PIN = 4;
   private static SSD1306 Display;  
   private static ArrayList<Integer> motionSensorReadings = new ArrayList<>();
   private static final int MAX_READINGS = 100; /


   public static void main(String[] args) {
       String portName = "COM3"; // Change COM port as needed
       arduino = new FirmataDevice(portName);
       try {
           arduino.start();
           arduino.ensureInitializationIsDone();
           System.out.println("Connected to Arduino on " + portName);
           I2CDevice i2cObject = arduino.getI2CDevice((byte) 0x3C);  // OLED I2C address
           Display = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
           Display.init();
           Display.getCanvas().setTextsize(1);
           Display.getCanvas().drawString(0, 0, "EECS Bonus Project");
           Display.getCanvas().drawString(0, 16, "Alarm System");
           Display.getCanvas().drawString(0, 32, "By: Kenneth Ly");
           Display.getCanvas().drawString(0, 48, "Number: 221324587");
           Display.display();
           Thread.sleep(2000);
           Display.clear();
       } catch (IOException | InterruptedException e) {
           System.err.println("Failed to connect to Arduino: " + e.getMessage());
           return;
       }
       // Start a separate thread for user input
       Thread userInputThread = new Thread(HomeAlarm::listenForUserInput);
       userInputThread.start();
       while (running) { 
           if (alarmActive) {
               setLight(true);
               checkMotionSensor();
           } else {
               setLight(false);
           }
           try {
               Thread.sleep(500); 
           } catch (InterruptedException e) {
               System.err.println("Error in main loop: " + e.getMessage());
           }
       }
       shutdownSystem(); 
   }


   private static void checkMotionSensor() {
       try {
           Pin motionPin = arduino.getPin(MOTION_PIN);
           int sensorValue = (int) motionPin.getValue();
           motionSensorReadings.add(sensorValue); 
           // Keep only the last MAX_READINGS
           if (motionSensorReadings.size() > MAX_READINGS) {
               motionSensorReadings.remove(0);
           }
           if (sensorValue < 600) {
               handleMotionDetection();
               Thread.sleep(1000);
           } else {
               System.out.println("No Motion Detected");
           }
           //For demonstration purposes, print the ArrayList's size
           System.out.println("Number of sensor readings: " + motionSensorReadings.size());
       } catch (Exception e) {
           System.err.println("Error reading motion sensor: " + e.getMessage());
       }
   }


   private static void handleMotionDetection() {
       System.out.println("Motion Detected");
       if (buzzerCount < 1) {
           try {
               Pin soundPin = arduino.getPin(SOUND_PIN);
               Pin lightPin = arduino.getPin(LIGHT_PIN);
               updateDisplay("ALARM TRIGGERED");
               Display.display();
               for (int i = 0; i < 9; i++) {
                   soundPin.setValue(1);
                   lightPin.setValue(0);
                   Thread.sleep(200);
                   soundPin.setValue(0);
                   lightPin.setValue(1);
                   Thread.sleep(200);
               }
               updateDisplay("ALARM: ON");
               buzzerCount++;
           } catch (Exception e) {
               System.err.println("Error handling motion detection: " + e.getMessage());
           }
       } else {
           System.out.println("Alarm Stopped");
           buzzerCount = 0;
           setLight(false);
       }
   }


   private static void setLight(boolean state) {
       try {
           Pin lightPin = arduino.getPin(LIGHT_PIN);
           lightPin.setValue(state ? 1 : 0);
       } catch (Exception e) {
           System.err.println("Error setting light state: " + e.getMessage());
       }
   }


   private static void listenForUserInput() {
       Scanner scanner = new Scanner(System.in);
       while (running) {
           System.out.print("Type 'on' to activate alarm, 'off' to deactivate, 'exit' to stop: ");
           String input = scanner.nextLine().trim().toLowerCase();
           if ("on".equals(input)) {
               alarmActive = true;
               System.out.println("Alarm Activated");
               updateDisplay("ALARM: ON");
           } else if ("off".equals(input)) {
               alarmActive = false;
               System.out.println("Alarm Deactivated");
               updateDisplay("ALARM: OFF");
           } else if ("exit".equals(input)) {
               running = false;
               System.out.println("Shutting down system...");
               Display.clear();
               break; // Exit loop and stop user input thread
           } else {
               System.out.println("Invalid command. Please type 'on', 'off', or 'exit'.");
           }
       }
       scanner.close();
   }


   private static void updateDisplay(String message) {
       Display.clear();  // Clear the display before updating
       Display.getCanvas().drawString(0, 0, "Home Alarm System");
       Display.getCanvas().drawString(0, 16, message);  // Display ON/OFF status
       Display.display();
   }


   private static void shutdownSystem() {
       try {
           setLight(false); // Turn off light
           arduino.stop(); // Properly close the Arduino connection
           System.out.println("System shut down successfully.");
       } catch (Exception e) {
           System.err.println("Error shutting down system: " + e.getMessage());
       }
   }
}



