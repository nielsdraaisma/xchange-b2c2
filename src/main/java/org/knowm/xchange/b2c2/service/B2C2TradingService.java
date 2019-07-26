package org.knowm.xchange.b2c2.service;

import org.knowm.xchange.b2c2.B2C2Adapters;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.*;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.*;

import java.io.IOException;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class B2C2TradingService extends B2C2TradingServiceRaw implements TradeService {
  public B2C2TradingService(B2C2Exchange exchange) {
    super(exchange);
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    OrderRequest orderRequest =
        new OrderRequest(
            UUID.randomUUID().toString(),
            limitOrder.getOriginalAmount().stripTrailingZeros().toPlainString(),
            B2C2Adapters.adaptSide(limitOrder.getType()),
            B2C2Adapters.adaptCurrencyPairToSpotInstrument(limitOrder.getCurrencyPair()),
            limitOrder.getLimitPrice().stripTrailingZeros().toPlainString(),
            false,
            DateTimeFormatter.ISO_DATE_TIME.format(
                ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(15)));

    // Use the order api to place the order using a random UUID as client reference.
    OrderResponse orderResponse = order(orderRequest);

    if (orderResponse.trades.size() == 1) {
      return orderResponse.trades.get(0).tradeId;
    } else {
      throw new IllegalStateException(
          "Did not get expected number of trades from B2C2 order response, expected 1 got "
              + orderResponse.trades.size());
    }
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    try {
      quoteLock.lock();
      final QuoteResponse quoteResponse =
          quote(
              new QuoteRequest(
                  toApiInstrument(marketOrder.getCurrencyPair()),
                  OrderSide.of(marketOrder.getType()).name().toLowerCase(),
                  marketOrder
                      .getOriginalAmount()
                      .setScale(4, RoundingMode.HALF_UP)
                      .toPlainString()));

      TradeResponse tradeResponse =
          trade(
              new TradeRequest(
                  quoteResponse.rfqId,
                  quoteResponse.quantity,
                  quoteResponse.side,
                  quoteResponse.instrument,
                  quoteResponse.price));
      return tradeResponse.tradeId;
    } finally {
      quoteLock.unlock();
    }
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    if (!(params instanceof B2C2TradeHistoryParams)) {
      throw new IllegalArgumentException("Invalid params given");
    }
    final B2C2TradeHistoryParams b2C2TradeHistoryParams = (B2C2TradeHistoryParams) params;
    List<UserTrade> userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(getLedger(b2C2TradeHistoryParams.offset, b2C2TradeHistoryParams.limit , "trade", b2C2TradeHistoryParams.since));

    userTrades =
        userTrades.stream()
            .filter(
                ut ->
                    b2C2TradeHistoryParams.getCurrencyPair() == null
                        || ut.getCurrencyPair().equals(b2C2TradeHistoryParams.getCurrencyPair()))
            .filter(
                ut ->
                    b2C2TradeHistoryParams.getTransactionId() == null
                        || b2C2TradeHistoryParams.getTransactionId().equals(ut.getOrderId()))
            .collect(Collectors.toList());

    return new UserTrades(
        userTrades, Trades.TradeSortType.SortByTimestamp); // TODO : Validate sort type
  }

  @Override
  /**
   * this expects a B2C2 trade no as returned by the placeOrder methods, not the b2c2 order id as
   * used on the /order endpoints.
   */
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    if (orderIds.length > 1) {
      throw new IllegalArgumentException("Multiple orderIds not supported");
    }
    final String tradeId = orderIds[0];
    return Collections.singletonList(B2C2Adapters.adoptTradeResponseToOrder(getTrade(tradeId)));
    // return Collections.singletonList(B2C2Adapters.adaptOrderResponseToOrder(getOrder(orderId)));
    //    return B2C2Adapters.adaptLedgerItemToUserTrades(getLedger()).stream()
    //        .filter(ut -> ut.getOrderId().equals(orderId))
    //        .map(
    //            ut ->
    //                new LimitOrder.Builder(ut.getType(), ut.getCurrencyPair())
    //                    .originalAmount(ut.getOriginalAmount())
    //                    .id(ut.getOrderId())
    //                    .timestamp(ut.getTimestamp())
    //                    .limitPrice(ut.getPrice())
    //                    .averagePrice(ut.getPrice())
    //                    .orderStatus(Order.OrderStatus.FILLED)
    //                    .build())
    //        .collect(Collectors.toList());
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new B2C2TradeHistoryParams();
  }

  public static class B2C2TradeHistoryParams
      implements TradeHistoryParams, TradeHistoryParamTransactionId, TradeHistoryParamCurrencyPair, TradeHistoryParamOffset, TradeHistoryParamLimit, TradeHistoryParamsTimeSpan {
    private CurrencyPair currencyPair;
    private String transactionId;
    private Long offset;
    private Integer limit;
    private Date since;

    @Override
    public CurrencyPair getCurrencyPair() {
      return currencyPair;
    }

    public void setCurrencyPair(CurrencyPair currencyPair) {
      this.currencyPair = currencyPair;
    }

    @Override
    public String getTransactionId() {
      return transactionId;
    }

    @Override
    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    @Override
    public Long getOffset() {
      return offset;
    }

    @Override
    public void setOffset(Long offset) {
      this.offset = offset * 2;
    }

    @Override
    public Integer getLimit() {
      return limit;
    }

    @Override
    public void setLimit(Integer limit) {
      this.limit = limit * 2;
    }

    @Override
    public Date getStartTime() {
      return since;
    }

    @Override
    public void setStartTime(Date startTime) {
      this.since = startTime;
    }

    @Override
    public Date getEndTime() {
      return null;
    }

    @Override
    public void setEndTime(Date endTime) {
      throw new NotAvailableFromExchangeException();
    }
  }
}
