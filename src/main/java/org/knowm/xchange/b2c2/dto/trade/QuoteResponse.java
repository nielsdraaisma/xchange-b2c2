package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class QuoteResponse {
  public final String validUntil;
  public final UUID rfqId;
  public final UUID clientRfqId;
  public final String quantity;
  public final String side;
  public final String instrument;
  public final String price;
  public final String created;

  public QuoteResponse(
      @JsonProperty("valid_until") String validUntil,
      @JsonProperty("rfq_id") UUID rfqId,
      @JsonProperty("client_rfq_id") UUID clientRfqId,
      @JsonProperty("quantity") String quantity,
      @JsonProperty("side") String side,
      @JsonProperty("instrument") String instrument,
      @JsonProperty("price") String price,
      @JsonProperty("created") String created) {
    this.validUntil = validUntil;
    this.rfqId = rfqId;
    this.clientRfqId = clientRfqId;
    this.quantity = quantity;
    this.side = side;
    this.instrument = instrument;
    this.price = price;
    this.created = created;
  }
}
