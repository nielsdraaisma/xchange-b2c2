package org.knowm.xchange.b2c2.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.Instrument;
import org.knowm.xchange.b2c2.dto.trade.QuoteRequest;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class B2C2MarketDataServiceRaw extends B2C2BaseServiceRaw implements MarketDataService {
  B2C2MarketDataServiceRaw(B2C2Exchange exchange) {
    super(exchange);
  }

  QuoteResponse getQuote(CurrencyPair currencyPair, Order.OrderType orderType, BigDecimal amount)
      throws IOException {
    String side;
    if (orderType == Order.OrderType.ASK) {
      side = "sell";
    } else if (orderType == Order.OrderType.BID) {
      side = "buy";
    } else {
      throw new IllegalArgumentException("Invalid orderType : " + orderType);
    }
    final QuoteRequest quoteRequest =
        new QuoteRequest(toApiInstrument(currencyPair), side, amount.toString());
    try {
      quoteLock.lock();
      return quote(quoteRequest);
    } catch (B2C2Exception e) {
      throw handleException(e);
    } finally {
      quoteLock.unlock();
    }
  }

  List<Instrument> getInstruments() throws IOException {
    try {
      if (authorizationHeader != null) {
        return b2c2.instruments(authorizationHeader);
      } else return Collections.emptyList();
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }
}
