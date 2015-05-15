/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.devices.iot.desktop;

import java.util.Random;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author Eduardo Moranchel <emoranchel@asmatron.org>
 */
public class Main {

  public static void main(String[] args) {
    Client client = ClientBuilder.newClient();
    WebTarget webTarget = client.target("http://localhost:8080/SensorPanel/rest/sensors/");
    webTarget.path("test").request().post(Entity.text(new Random().nextInt(100)));
  }
}
