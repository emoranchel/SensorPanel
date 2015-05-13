package org.devices.iot.sensorpanel;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationConfig extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> resources = new java.util.HashSet<>();
    addRestResourceClasses(resources);
    return resources;
  }

  private void addRestResourceClasses(Set<Class<?>> resources) {
    resources.add(org.devices.iot.sensorpanel.SensorsResource.class);
  }

}
