/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iot.stub;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
public class RESTRunner implements Runner {
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
    System.out.println("Creating HTTP connection...");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://192.168.1.100:8080/SensorPanel/rest/sensors/");
    System.out.println("Opening GPIO...");
    try (GPIOPin led17 = DeviceManager.open(GPIOPin.class, led17config);
            GPIOPin led4 = DeviceManager.open(GPIOPin.class, led4config);
            GPIOPin led22 = DeviceManager.open(GPIOPin.class, led22config)) {
      System.out.println("Main loop started.");
      while (running.get()) {
        String response = target.path("Temperature")
                .request(javax.ws.rs.core.MediaType.TEXT_PLAIN)
               .get(String.class);
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

}
