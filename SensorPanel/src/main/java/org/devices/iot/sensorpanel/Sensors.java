package org.devices.iot.sensorpanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named
public class Sensors {

  private final Map<String, String> sensors = new HashMap<>();

  public synchronized String get(String sensor) {
    return sensors.get(sensor);
  }

  public synchronized void set(String sensor, String value) {
    sensors.put(sensor, value);
  }

  public synchronized List<String> getSensors() {
    return sensors.entrySet()
            .stream()
            .map((entry) -> {
              return entry.getKey() + "=" + entry.getValue();
            })
            .collect(Collectors.toList());
  }
}
