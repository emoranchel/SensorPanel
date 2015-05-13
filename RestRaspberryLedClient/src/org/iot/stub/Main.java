package org.iot.stub;

import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
/*
 -Djava.library.path="/home/pi/dio/dev-26d04027287d/build/so" -Djava.security.policy="/home/pi/dio/dio.policy"
 */

public class Main {

  public void run(AtomicBoolean running) throws Exception {
    System.out.println("Creating HTTP connection...");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://192.168.1.100:8080/SensorPanel/rest/sensors/");
    System.out.println("Opening GPIO...");
// Yellow led
    GPIOPinConfig led17config = new GPIOPinConfig(
            DeviceConfig.DEFAULT,
            17,
            GPIOPinConfig.DIR_OUTPUT_ONLY,
            GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
            GPIOPinConfig.TRIGGER_NONE,
            false
    );
// Red led
    GPIOPinConfig led4config = new GPIOPinConfig(
            DeviceConfig.DEFAULT,
            4,
            GPIOPinConfig.DIR_OUTPUT_ONLY,
            GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
            GPIOPinConfig.TRIGGER_NONE,
            false
    );
// Blue led
    GPIOPinConfig led22config = new GPIOPinConfig(
            DeviceConfig.DEFAULT,
            22,
            GPIOPinConfig.DIR_OUTPUT_ONLY,
            GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
            GPIOPinConfig.TRIGGER_NONE,
            false
    );

    try (GPIOPin led17 = DeviceManager.open(GPIOPin.class, led17config);
            GPIOPin led4 = DeviceManager.open(GPIOPin.class, led4config);
            GPIOPin led22 = DeviceManager.open(GPIOPin.class, led22config)) {
      System.out.println("Main loop started.");
      while (running.get()) {
//        String response = target.path("Temperature")
//                .request(javax.ws.rs.core.MediaType.TEXT_PLAIN)
//                .get(String.class);
//        double temp = Double.parseDouble(response);
        double temp = new Random().nextInt(40);
        System.out.format("Temperature from REST service: %.2f", temp);
        if (temp > 30) {
          led17.setValue(false);
          led22.setValue(false);
          led4.setValue(true);
        } else if (temp < 10) {
          led17.setValue(false);
          led22.setValue(true);
          led4.setValue(false);
        } else {
          led17.setValue(true);
          led22.setValue(false);
          led4.setValue(false);
        }
        Thread.sleep(1000);
      }
      led17.setValue(false);
      led4.setValue(false);
      led22.setValue(false);
    }
  }

  public static void main(String[] args) throws Exception {
    Logger.getLogger("DIO").setLevel(Level.WARNING);
    Logger.getLogger("GrovePi").setLevel(Level.WARNING);
    Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

    File control = new File("LOCKFILE");
    control.deleteOnExit();
    if (control.exists()) {
      System.out.println("Stopping App");
      control.delete();
      System.exit(0);
    }
    System.out.println("Starting App");

    control.createNewFile();

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
        main.run(running);
      } catch (Exception ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
      while (control.exists()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
      }
      running.set(false);
      lock.release();
    });

    lock.acquire();
    System.out.println("Application Shutting Down Now!");
    running.set(false);
    try {
      control.delete();
      runner.shutdown();
      consoleMonitor.shutdownNow();
      fileMonitor.shutdownNow();
      runner.awaitTermination(10, TimeUnit.SECONDS);
    } catch (Exception e) {
    }
    System.exit(0);
  }

}
