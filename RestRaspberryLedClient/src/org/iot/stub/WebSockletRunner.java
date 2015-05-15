/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iot.stub;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import org.glassfish.tyrus.client.ClientManager;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
@ClientEndpoint
public class WebSockletRunner implements Runner {

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

  @Override
  public void run(AtomicBoolean running) throws Exception {

    System.out.println("Opening GPIO...");
    ClientManager client = ClientManager.createClient();

    try (GPIOPin led17 = DeviceManager.open(GPIOPin.class, led17config);
            GPIOPin led4 = DeviceManager.open(GPIOPin.class, led4config);
            GPIOPin led22 = DeviceManager.open(GPIOPin.class, led22config);
            Session websocket = client.connectToServer(new LedManager(led17, led4, led22), URI.create("ws://192.168.1.10:8080/SensorPanel/ws/notifications/temperature"))) {
      System.out.println("Main loop started.");
      while (running.get()) {
        Thread.sleep(1000);
      }
      led17.setValue(false);
      led4.setValue(false);
      led22.setValue(false);
    }
  }

  @ClientEndpoint
  private static class LedManager {

    private final GPIOPin yellowLed;
    private final GPIOPin redLed;
    private final GPIOPin blueLed;

    private LedManager(GPIOPin yellowLed, GPIOPin redLed, GPIOPin blueLed) {
      this.yellowLed = yellowLed;
      this.redLed = redLed;
      this.blueLed = blueLed;
    }

    @OnMessage
    public void onMessage(String value) {
      double temp = Double.parseDouble(value);
      System.out.format("Temperature from Random service: %.2f", temp);
      try {
        if (temp > 30) {
          yellowLed.setValue(false);
          blueLed.setValue(false);
          redLed.setValue(true);
        } else if (temp < 10) {
          yellowLed.setValue(false);
          blueLed.setValue(true);
          redLed.setValue(false);
        } else {
          yellowLed.setValue(true);
          blueLed.setValue(false);
          redLed.setValue(false);
        }
      } catch (IOException ex) {
        Logger.getLogger(WebSockletRunner.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

}
