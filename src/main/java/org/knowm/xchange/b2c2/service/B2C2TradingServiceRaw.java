package org.knowm.xchange.b2c2.service;

import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.OrderRequest;
import org.knowm.xchange.b2c2.dto.trade.OrderResponse;
import org.knowm.xchange.b2c2.dto.trade.TradeRequest;
import org.knowm.xchange.b2c2.dto.trade.TradeResponse;

import java.io.IOException;

public class B2C2TradingServiceRaw extends B2C2BaseServiceRaw {

  B2C2TradingServiceRaw(B2C2Exchange exchange) {
    super(exchange);
  }

  public OrderResponse order(OrderRequest orderRequest) throws IOException {
    try {
      return this.b2c2.order(this.authorizationHeader, orderRequest);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }

  public OrderResponse getOrder(String id) throws IOException {
    try {
      return this.b2c2.getOrder(this.authorizationHeader, id);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }

  public TradeResponse trade(TradeRequest tradeRequest) throws IOException {
    try {
      return this.b2c2.trade(this.authorizationHeader, tradeRequest);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }
}
