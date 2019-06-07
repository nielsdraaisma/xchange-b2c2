package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeResponse {
  public final String created;
  public final String instrument;
  public final String order;
  public final String price;
  public final String quantity;
  public final String rfqId;
  public final String side;
  public final String tradeId;

  public TradeResponse(
      @JsonProperty("created") String created,
      @JsonProperty("instrument") String instrument,
      @JsonProperty("order") String order,
      @JsonProperty("price") String price,
      @JsonProperty("quantity") String quantity,
      @JsonProperty("rfq_id") String rfqId,
      @JsonProperty("side") String side,
      @JsonProperty("trade_id") String tradeId) {
    this.created = created;
    this.instrument = instrument;
    this.order = order;
    this.price = price;
    this.quantity = quantity;
    this.rfqId = rfqId;
    this.side = side;
    this.tradeId = tradeId;
  }
}
