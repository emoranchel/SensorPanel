package org.iot.stub;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.iot.raspberry.grovepi.GroveDigitalIn;
import org.iot.raspberry.grovepi.GroveDigitalOut;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.devices.GroveLightSensor;
import org.iot.raspberry.grovepi.devices.GroveRgbLcd;
import org.iot.raspberry.grovepi.devices.GroveRotarySensor;
import org.iot.raspberry.grovepi.devices.GroveRotaryValue;
import org.iot.raspberry.grovepi.devices.GroveSoundSensor;
import org.iot.raspberry.grovepi.devices.GroveTemperatureAndHumiditySensor;
import org.iot.raspberry.grovepi.devices.GroveTemperatureAndHumidityValue;
import org.iot.raspberry.grovepi.devices.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.dio.GrovePiDio;

public class Main {

  private void open(GrovePi grovePi) throws IOException {
    System.out.println("Opening Sensors");
    lightSensor = new GroveLightSensor(grovePi, 0);
    potenciometer = new GroveRotarySensor(grovePi, 1);
    soundSensor = new GroveSoundSensor(grovePi, 2);
    button = grovePi.getDigitalIn(3);
    tempSensor = new GroveTemperatureAndHumiditySensor(grovePi, 4, GroveTemperatureAndHumiditySensor.Type.DHT11);
    ranger = new GroveUltrasonicRanger(grovePi, 5);

    redLed = grovePi.getDigitalOut(6);
    blueLed = grovePi.getDigitalOut(7);

    button.setListener((oldVal, newVal) -> send("button", Boolean.toString(newVal)));

    lcd = grovePi.getLCD();
    System.out.println("Opening HTTP client");
    Client client = ClientBuilder.newClient();
    webTarget = client.target("http://192.168.1.100:8080/SensorPanel/rest/sensors/");
    httpExecutor = Executors.newSingleThreadExecutor();

    buttonMonitor = Executors.newSingleThreadScheduledExecutor();
    buttonMonitor.scheduleAtFixedRate(button, 0, 100, TimeUnit.MILLISECONDS);
  }
  private ScheduledExecutorService buttonMonitor;
  private ExecutorService httpExecutor;
  private WebTarget webTarget;

  private GroveDigitalOut blueLed;
  private GroveDigitalOut redLed;
  private GroveDigitalIn button;
  private GroveRgbLcd lcd;
  private GroveUltrasonicRanger ranger;
  private GroveLightSensor lightSensor;
  private GroveTemperatureAndHumiditySensor tempSensor;
  private GroveRotarySensor potenciometer;
  private GroveSoundSensor soundSensor;

  public void run(GrovePi grovePi, AtomicBoolean running) throws Exception {
    System.out.println("Starting up");
    while (running.get()) {
      System.out.println("In main loop");
      try {
        lcd.setRGB(80, 150, 80);
        send("ultrasonicRanger", String.format("%.2f", ranger.get()));
        send("light", String.format("%.2f", lightSensor.get()));
        send("sound", String.format("%.2f", soundSensor.get()));
        GroveRotaryValue potenciometerVal = potenciometer.get();
        send("potenciometer_degrees", String.format("%.2f", potenciometerVal.getDegrees()));
        send("potenciometer_factor", String.format("%.2f", potenciometerVal.getFactor()));
        send("potenciometer_sensorValue", String.format("%.2f", potenciometerVal.getSensorValue()));
        send("potenciometer_voltage", String.format("%.2f", potenciometerVal.getVoltage()));
        GroveTemperatureAndHumidityValue dht = tempSensor.get();
        send("temperature", String.format("%.2f", dht.getTemperature()));
        send("humidity", String.format("%.2f", dht.getHumidity()));
        blueLed.set(true);
        redLed.set(false);
        lcd.setRGB(100, 100, 255);
        lcd.setText("waiting...");
        Thread.sleep(3000);
      } catch (IOException ex) {
        System.out.println("IO ERROR");
        blueLed.set(false);
        redLed.set(true);
        lcd.setRGB(255, 0, 0);
        lcd.setText("ERROR");
        System.out.println("Error");
      } catch (InterruptedException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private void send(String sensor, String value) {
    try {
      String message = sensor + "=" + value;
      System.out.println(message);
      lcd.setText(message);
      Thread.sleep(1000);
    } catch (IOException | InterruptedException ex) {
    }
    httpExecutor.execute(() -> {
      try {
        webTarget.path(sensor).request().post(Entity.text(value));
      } catch (Exception ex) {
        try {
          System.out.println("HTTP ERROR");
          ex.printStackTrace();
          lcd.setText("Error:" + sensor);
        } catch (IOException ex1) {
        }
      }
    });
  }

  private void close(GrovePi grovePi) throws IOException {
    System.out.println("finalizing stuff.");
    buttonMonitor.shutdown();
    httpExecutor.shutdownNow();
    blueLed.set(false);
    redLed.set(false);

  }

  public static void main(String[] args) throws Exception {
    Logger.getLogger("DIO").setLevel(Level.WARNING);
    Logger.getLogger("GrovePi").setLevel(Level.WARNING);
    Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

    File control = new File("LOCKFILE");
    control.deleteOnExit();
    if (control.exists()) {
      System.out.println("STOPPING CURRENT INSTANCE");
      control.delete();
      System.exit(0);
    }

    System.out.println("Starting system...");

    control.createNewFile();

    GrovePi grovePi = new GrovePiDio();

    //Your project runner
    final ExecutorService runner = Executors.newSingleThreadExecutor();
    //The console monitor
    final ExecutorService consoleMonitor = Executors.newSingleThreadExecutor();
    //The file lock monitor
    final ExecutorService fileMonitor = Executors.newSingleThreadExecutor();

    final Semaphore lock = new Semaphore(0);
    final AtomicBoolean running = new AtomicBoolean(true);

    runner.execute(() -> {
      Main main = new Main();
      try {
        main.open(grovePi);
        main.run(grovePi, running);

      } catch (Exception ex) {
        Logger.getLogger(Main.class
                .getName()).log(Level.SEVERE, null, ex);
      }
      try {
        main.close(grovePi);

      } catch (Exception ex) {
        Logger.getLogger(Main.class
                .getName()).log(Level.SEVERE, null, ex);
      }
      lock.release();
    });

    consoleMonitor.execute(() -> {
      try (Scanner scanner = new Scanner(System.in)) {
        String command;
        while (!(command = scanner.next()).equalsIgnoreCase("quit")) {
          System.out.println("Command " + command + " not recognized, try quit");
        }
      }
      running.set(false);
      lock.release();
    });

    fileMonitor.execute(() -> {
      try {
        while (control.exists()) {
          Thread.sleep(100);
        }
      } catch (InterruptedException ex) {
      }
      running.set(false);
      lock.release();
    });

    lock.acquire();
    System.out.println("Application shutting down now!");
    try {
      control.delete();
      running.set(false);
      runner.shutdown();
      consoleMonitor.shutdownNow();
      fileMonitor.shutdownNow();
      runner.awaitTermination(10, TimeUnit.SECONDS);
    } catch (Exception e) {
    }
    try {
      grovePi.close();
    } catch (Exception e) {
    }

    System.exit(0);
  }

}
