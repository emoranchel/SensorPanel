package org.devices.iot.sensorpanel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.Session;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
@ApplicationScoped
public class NotificationSessionManager {

  private Set<Session> sessions = new HashSet<>();

  public synchronized void add(String sensor, Session session) {
    if (sensor != null) {
      session.getUserProperties().put("sensor", sensor);
    }
    sessions.add(session);
  }

  public synchronized void remove(Session session) {
    sessions.remove(session);
  }

  public synchronized void onSensorUpdate(@Observes @SensorUpdate SensorUpdateEvent event) {
    sessions.stream().filter((session) -> {
      String sensor = (String) session.getUserProperties().get("sensor");
      return sensor == null || sensor.equals(event.getSensor());
    }).forEach((session) -> {
      try {
        String sensor = (String) session.getUserProperties().get("sensor");
        if (sensor == null) {
          session.getBasicRemote().sendText(event.getSensor() + "|" + event.getValue());
        } else {
          session.getBasicRemote().sendText(event.getValue());
        }
      } catch (IOException ex) {
        Logger.getLogger(NotificationSessionManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    });
  }

}
