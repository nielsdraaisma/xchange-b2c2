package org.knowm.xchange.b2c2;

import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.OrderResponse;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.b2c2.dto.trade.TradeResponse;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class B2C2Adapters {

  private static final Set<Currency> cryptos = new HashSet<>();
  private static final Map<String, CurrencyPair> instrumentsToCurrencypairs =
      new ConcurrentHashMap<>();
  private static final Map<CurrencyPair, String> currencyPairsToInstruments =
      new ConcurrentHashMap<>();

  static {
    cryptos.add(Currency.BTC);
    cryptos.add(Currency.BCH);
    cryptos.add(Currency.ETH);
    cryptos.add(Currency.XRP);
    cryptos.add(Currency.LTC);
    cryptos.add(Currency.EOS);
  }

  private B2C2Adapters() {}

  static boolean isPositive(final String string) {
    return isPositive(new BigDecimal(string));
  }

  static boolean isPositive(final BigDecimal bigDecimal) {
    return bigDecimal.abs().equals(bigDecimal);
  }

  static boolean isCrypto(final Currency currency) {
    return cryptos.contains(currency);
  }

  public static Order.OrderType adaptSide(final String side) {
    if (side.equals("buy")) {
      return Order.OrderType.BID;
    } else if (side.equals("sell")) {
      return Order.OrderType.ASK;
    } else {
      throw new IllegalArgumentException("Cannot adapt side " + side);
    }
  }

  public static String adaptSide(final Order.OrderType type) {
    if (type == Order.OrderType.ASK || type == Order.OrderType.EXIT_ASK) {
      return "sell";
    } else if (type == Order.OrderType.BID || type == Order.OrderType.EXIT_BID) {
      return "buy";
    } else {
      throw new IllegalArgumentException("Cannot adapt orderType " + type);
    }
  }

  public static CurrencyPair adaptInstrumentToCurrencyPair(final String instrument) {
    instrumentsToCurrencypairs.computeIfAbsent(
        instrument,
        i -> {
          String base = instrument.substring(0, 3);
          String counter = instrument.substring(3, 6);
          return new CurrencyPair(new Currency(base), new Currency(counter));
        });
    return instrumentsToCurrencypairs.get(instrument);
  }

  public static String adaptCurrencyPairToSpotInstrument(final CurrencyPair currencyPair) {
    currencyPairsToInstruments.computeIfAbsent(
        currencyPair,
        i -> currencyPair.base.toString() + currencyPair.counter.toString() + ".SPOT");
    return currencyPairsToInstruments.get(currencyPair);
  }

  private static Date nullableStringToDate(String s) {
    return Optional.ofNullable(s)
        .map(ZonedDateTime::parse)
        .map(ZonedDateTime::toInstant)
        .map(Date::from)
        .orElse(null);
  }

  public static Ticker adaptQuote(CurrencyPair currencyPair, QuoteResponse quoteResponse) {
    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .timestamp(nullableStringToDate(quoteResponse.created))
        .last(new BigDecimal(quoteResponse.price))
        .askSize(new BigDecimal(quoteResponse.quantity))
        .build();
  }

  public static FundingRecord adaptLedgerItemToFundingRecord(LedgerItem item) {
    return new FundingRecord.Builder()
        .setInternalId(item.transactionId)
        .setDate(nullableStringToDate(item.created))
        .setDescription(item.reference)
        .setCurrency(new Currency(item.currency))
        .setAmount(new BigDecimal(item.amount))
        .build();
  }

  public static List<UserTrade> adaptLedgerItemToUserTrades(final List<LedgerItem> items) {
    final List<UserTrade> userTrades = new ArrayList<>();
    // Get all unique references, a trade has two ledger entries which share the same reference
    final Set<String> tradeReferences =
        items.stream()
            .filter(i -> i.type.equals("trade"))
            .map(i -> i.reference)
            .collect(Collectors.toSet());
    // Attempt to create UserTrade for each reference
    for (String reference : tradeReferences) {
      final List<LedgerItem> matchingLedgerItems =
          items.stream().filter(i -> i.reference.equals(reference)).collect(Collectors.toList());
      final LedgerItem positive =
          matchingLedgerItems.stream().filter(i -> isPositive(i.amount)).findFirst().orElse(null);
      final LedgerItem negative =
          matchingLedgerItems.stream().filter(i -> !isPositive(i.amount)).findFirst().orElse(null);

      // We require both sides of the trade to be present
      if (positive == null || negative == null) {
        break;
      }
      final CurrencyPair currencyPair;
      final Order.OrderType orderType;
      final BigDecimal originalAmount;
      final BigDecimal price;
      if (isCrypto(new Currency(negative.currency))) {
        orderType = Order.OrderType.ASK;
        originalAmount = new BigDecimal(negative.amount).abs();
        currencyPair =
            new CurrencyPair(new Currency(negative.currency), new Currency(positive.currency));
        price = new BigDecimal(positive.amount).divide(originalAmount, RoundingMode.HALF_UP);
      } else {
        orderType = Order.OrderType.BID;
        originalAmount = new BigDecimal(positive.amount);
        currencyPair =
            new CurrencyPair(new Currency(positive.currency), new Currency(negative.currency));
        price = new BigDecimal(negative.amount).abs().divide(originalAmount, RoundingMode.HALF_UP);
      }

      userTrades.add(
          new UserTrade.Builder()
              .timestamp(nullableStringToDate(positive.created))
              .feeAmount(BigDecimal.ZERO)
              .currencyPair(currencyPair)
              .originalAmount(originalAmount)
              .price(price)
              .orderId(reference)
              .type(orderType)
              .build());
    }
    return userTrades;
  }

  public static Order adaptOrderResponseToOrder(OrderResponse order) {
    LimitOrder.Builder builder =
        new LimitOrder.Builder(
                adaptSide(order.side), adaptInstrumentToCurrencyPair(order.instrument))
            .originalAmount(order.quantity)
            .id(order.clientOrderId)
            .limitPrice(order.price)
            .timestamp(nullableStringToDate(order.created));
    if (order.executedPrice != null) {
      builder =
          builder
              .cumulativeAmount(order.quantity)
              .averagePrice(order.executedPrice)
              .limitPrice(order.price);
    }
    return builder.build();
  }

  public static Order adoptTradeResponseToOrder(TradeResponse trade) {
    return new LimitOrder.Builder(
            adaptSide(trade.side), adaptInstrumentToCurrencyPair(trade.instrument))
        .timestamp(nullableStringToDate(trade.created))
        .limitPrice(new BigDecimal(trade.price))
        .averagePrice(new BigDecimal(trade.price))
        .originalAmount(new BigDecimal(trade.quantity))
        .id(trade.tradeId)
        .build();
  }
}
