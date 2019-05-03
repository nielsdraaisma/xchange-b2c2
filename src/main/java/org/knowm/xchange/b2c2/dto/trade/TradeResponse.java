package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeResponse {
  public final String trade_id;

  public TradeResponse(@JsonProperty("trade_id") String trade_id) {
    this.trade_id = trade_id;
  }
}
