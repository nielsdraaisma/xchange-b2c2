package org.knowm.xchange.b2c2.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.knowm.xchange.b2c2.B2C2Adapters;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.Instrument;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class B2C2MarketDataService extends B2C2MarketDataServiceRaw implements MarketDataService {
  private static final Set<BigDecimal> quoteSizes = new HashSet<>();
  private static final Set<Order.OrderType> quoteTypes = new HashSet<>();

  static {
    quoteSizes.add(new BigDecimal("1"));
    quoteSizes.add(new BigDecimal("5"));
    quoteTypes.add(Order.OrderType.BID);
    quoteTypes.add(Order.OrderType.ASK);
  }

  public B2C2MarketDataService(B2C2Exchange exchange) {
    super(exchange);
  }

  @Override
  public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
    return B2C2Adapters.adaptQuote(
        currencyPair, super.getQuote(currencyPair, Order.OrderType.BID, BigDecimal.ONE));
  }

  @Override
  public OrderBook getOrderBook(final CurrencyPair currencyPair, Object... args)
      throws IOException {
    final List<LimitOrder> orders = new ArrayList<>();

    for (BigDecimal quoteSize : quoteSizes) {
      for (Order.OrderType orderType : quoteTypes) {
        orders.add(quoteSingleOrder(currencyPair, orderType, quoteSize));
      }
    }

    return new OrderBook(
        new Date(),
        orders.stream()
            .filter(o -> o.getType() == Order.OrderType.ASK)
            .collect(Collectors.toList()),
        orders.stream()
            .filter(o -> o.getType() == Order.OrderType.BID)
            .collect(Collectors.toList()));
  }

  private LimitOrder quoteSingleOrder(
      CurrencyPair currencyPair, Order.OrderType orderType, BigDecimal size) throws IOException {
    final QuoteResponse quoteResponse = getQuote(currencyPair, orderType, size);
    return new LimitOrder(
        orderType,
        size,
        currencyPair,
        quoteResponse.rfqId.toString(),
        new Date(),
        new BigDecimal(quoteResponse.price));
  }

  public List<Instrument> getInstruments() throws IOException {
    return super.getInstruments();
  }
}
