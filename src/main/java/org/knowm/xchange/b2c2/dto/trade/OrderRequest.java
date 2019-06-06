package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class OrderRequest {
//  @JsonProperty("client_order_id")
//  public final String clientOrderId;

  @JsonProperty("quantity")
  public final String quantity; // Max 4 decimals

  @JsonProperty("side")
  public final String side; // buy / sell

  @JsonProperty("instrument")
  public final String instrument;

  @JsonProperty("order_type")
  public final String orderType = "FOK"; // Only FOK allowed

  @JsonProperty("price")
  public final String price;

  @JsonProperty("force_open")
  public final Boolean forceOpen;

  @JsonProperty("valid_until")
  public final String validUntil;

  public OrderRequest(
//      String clientOrderId,
      String quantity,
      String side,
      String instrument,
      String price,
      Boolean forceOpen,
      String validUntil) {
//    this.clientOrderId = clientOrderId;
    this.quantity = quantity;
    this.side = side;
    this.instrument = instrument;
    this.price = price;
    this.forceOpen = forceOpen;
    this.validUntil = validUntil;
  }
}
