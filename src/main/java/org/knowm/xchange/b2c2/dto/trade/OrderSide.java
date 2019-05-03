package org.knowm.xchange.b2c2.dto.trade;

import org.knowm.xchange.dto.Order;

public enum OrderSide {
  BUY,
  SELL;

  public static OrderSide of(Order.OrderType orderType) {
    switch (orderType) {
      case BID:
        return BUY;
      case ASK:
        return SELL;
      default:
        return null;
    }
  }
}
