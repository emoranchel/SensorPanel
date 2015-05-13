package org.iot.stub;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  public void open() {

  }

  public void run(AtomicBoolean running) {
    //Your App goes here
  }

  public void close() {

  }

  public static void main(String[] args) throws Exception {
    Logger.getLogger("DIO").setLevel(Level.WARNING);
    Logger.getLogger("GrovePi").setLevel(Level.WARNING);
    Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

    File control = new File("LOCKFILE");
    control.deleteOnExit();
    if (control.exists()) {
      control.delete();
      System.exit(0);
    }

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
        main.open();
        main.run(running);
      } catch (Exception ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {
        main.close();
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
    running.set(false);
    runner.shutdown();
    consoleMonitor.shutdownNow();
    fileMonitor.shutdownNow();
    runner.awaitTermination(10, TimeUnit.SECONDS);
    control.delete();
    System.exit(0);
  }

}
