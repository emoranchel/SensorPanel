package org.devices.iot.sensorpanel;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
public class SensorUpdateEvent {

  private final String sensor;
  private final String value;

  public SensorUpdateEvent(String sensor, String value) {
    this.sensor = sensor;
    this.value = value;
  }

  public String getSensor() {
    return sensor;
  }

  public String getValue() {
    return value;
  }

}
