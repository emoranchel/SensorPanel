package org.iot.stub;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Runner {

  void run(AtomicBoolean running) throws Exception;
}
