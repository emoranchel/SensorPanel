package org.devices.iot.sensorpanel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
@ServerEndpoint("/ws/notify/{sensor}")
@RequestScoped
public class SensorNotifications {

  @Inject
  private NotificationSessionManager sessions;
  @Inject
  private Sensors sensors;

  @OnOpen
  public void onOpen(Session session, @PathParam("sensor") String sensor) {
    if ("ALL".equals(sensor)) {
      sensor = null;
    }
    send(sensor, session);
    sessions.add(sensor, session);
  }

  @OnMessage
  public String onMessage(String message) {
    return message;
  }

  @OnClose
  public void onClose(Session session) {
    sessions.remove(session);
  }

  private void send(String sensor, Session session) {
    sensors.getRawSensors().stream()
            .filter((s) -> {
              return sensor == null || sensor.equals(s[0]);
            })
            .forEach((s) -> {
              try {
                if (sensor == null) {
                  session.getBasicRemote().sendText(s[0] + "|" + s[1]);
                } else {
                  session.getBasicRemote().sendText(s[1]);
                }
              } catch (IOException ex) {
                Logger.getLogger(SensorNotifications.class.getName()).log(Level.SEVERE, null, ex);
              }
            });
  }

}
