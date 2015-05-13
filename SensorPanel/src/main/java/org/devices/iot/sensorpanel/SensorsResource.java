package org.devices.iot.sensorpanel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("sensors")
@ApplicationScoped
public class SensorsResource {

  @Inject
  private Sensors sensors;

  @GET
  @Produces("text/plain")
  @Path("/{sensor}")
  public String getText(@PathParam("sensor") String sensor) {
    return sensors.get(sensor);
  }

  @POST
  @Path("/{sensor}")
  @Consumes("text/plain")
  public void putText(String value, @PathParam("sensor") String sensor) {
    sensors.set(sensor, value);
  }
}
