package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Instrument {
  public final String name;

  public Instrument(@JsonProperty("name") String name) {
    this.name = name;
  }
}
