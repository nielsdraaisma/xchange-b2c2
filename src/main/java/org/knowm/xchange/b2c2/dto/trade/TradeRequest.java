package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class TradeRequest {
  @JsonProperty("rfq_id")
  public final UUID rfqId;

  @JsonProperty("quantity")
  public final String quantity;

  @JsonProperty("side")
  public final String side;

  @JsonProperty("instrument")
  public final String instrument;

  @JsonProperty("price")
  public final String price;

  public TradeRequest(UUID rfqId, String quantity, String side, String instrument, String price) {
    this.rfqId = rfqId;
    this.quantity = quantity;
    this.side = side;
    this.instrument = instrument;
    this.price = price;
  }
}
