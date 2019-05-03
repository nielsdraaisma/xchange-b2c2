package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class QuoteRequest {
  public final String instrument;
  public final String side;
  public final String quantity;

  @JsonProperty("client_rfq_id")
  public final UUID clientRfqId;

  public QuoteRequest(String instrument, String side, String quantity) {
    this.instrument = instrument;
    this.side = side;
    this.quantity = quantity;
    this.clientRfqId = UUID.randomUUID();
  }
}
