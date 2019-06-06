package org.knowm.xchange;

import org.junit.Test;
import org.knowm.xchange.b2c2.B2C2Adapters;
import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.OrderResponse;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.UserTrade;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class B2C2AdaptersTest {

  @Test
  public void testAdaptInstrumentToCurrencyPair() {
    assertThat(B2C2Adapters.adaptInstrumentToCurrencyPair("BTCUSD.SPOT"))
        .isEqualTo(CurrencyPair.BTC_USD);
    assertThat(B2C2Adapters.adaptInstrumentToCurrencyPair("BTCUSD.CFD"))
        .isEqualTo(CurrencyPair.BTC_USD);
  }

  @Test
  public void testAdaptQuote() {
    final QuoteResponse quoteResponse =
        new QuoteResponse(
            "2017-01-01T19:45:22.025464Z",
            UUID.fromString("d4e41399-e7a1-4576-9b46-349420040e1a"),
            UUID.fromString("149dc3e7-4e30-4e1a-bb9c-9c30bd8f5ec7"),
            "1.0000000000",
            "buy",
            "BTCUSD.SPOT",
            "700.00000000",
            "2018-02-06T16:07:50.122206Z");
    final Ticker ticker = B2C2Adapters.adaptQuote(CurrencyPair.BTC_AUD, quoteResponse);
    assertThat(ticker).isNotNull();
    assertThat(ticker.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_AUD);
    assertThat(ticker.getLast()).isEqualTo(new BigDecimal("700.00000000"));
    assertThat(ticker.getTimestamp())
        .isEqualTo(Date.from(ZonedDateTime.parse("2018-02-06T16:07:50.122206Z").toInstant()));
  }

  @Test
  public void testAdaptLedgerItemToFundingRecord() {}

  @Test
  public void testAdaptLedgerItemToUserTrade() {
    LedgerItem ledgerItem1 =
        new LedgerItem("tx1", "2018-02-06T16:07:50.122206Z", "ref", "USD", "-6000", "trade");
    LedgerItem ledgerItem2 =
        new LedgerItem("tx2", "2018-02-06T16:07:50.122206Z", "ref", "BTC", "1", "trade");
    List<LedgerItem> ledgerItems = new ArrayList<>();
    ledgerItems.add(ledgerItem1);
    ledgerItems.add(ledgerItem2);
    List<UserTrade> userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(ledgerItems);
    assertThat(userTrades).hasSize(1);
    UserTrade userTrade = userTrades.iterator().next();
    assertThat(userTrade.getOriginalAmount()).isEqualTo(new BigDecimal("1"));
    assertThat(userTrade.getPrice()).isEqualTo(new BigDecimal("6000"));
    assertThat(userTrade.getType()).isEqualTo(Order.OrderType.BID);
    assertThat(userTrade.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_USD);

    //     Change amount to -1, should now become a ask
    ledgerItem1 =
        new LedgerItem("tx1", "2018-02-06T16:07:50.122206Z", "ref", "USD", "6000", "trade");
    ledgerItem2 = new LedgerItem("tx2", "2018-02-06T16:07:50.122206Z", "ref", "BTC", "-1", "trade");
    ledgerItems.clear();
    ledgerItems.add(ledgerItem1);
    ledgerItems.add(ledgerItem2);

    userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(ledgerItems);
    userTrade = userTrades.iterator().next();
    assertThat(userTrade.getOriginalAmount()).isEqualTo(new BigDecimal("1"));
    assertThat(userTrade.getType()).isEqualTo(Order.OrderType.ASK);
    assertThat(userTrade.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_USD);
  }

  @Test
  public void testAdaptLedgerItemToUserTradeSetsTheCorrectPriceForBuy() {
    LedgerItem ledgerItem1 =
        new LedgerItem("tx1", "2018-02-06T16:07:50.122206Z", "ref", "USD", "-5000", "trade");
    LedgerItem ledgerItem2 =
        new LedgerItem("tx2", "2018-02-06T16:07:50.122206Z", "ref", "BTC", "0.5", "trade");
    List<LedgerItem> ledgerItems = new ArrayList<>();
    ledgerItems.add(ledgerItem1);
    ledgerItems.add(ledgerItem2);
    List<UserTrade> userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(ledgerItems);
    assertThat(userTrades).hasSize(1);
    UserTrade userTrade = userTrades.iterator().next();
    assertThat(userTrade.getPrice()).isEqualTo(new BigDecimal("10000"));
  }

  @Test
  public void testAdaptLedgerItemToUserTradeSetsTheCorrectPriceForSell() {
    LedgerItem ledgerItem1 =
            new LedgerItem("tx1", "2018-02-06T16:07:50.122206Z", "ref", "USD", "5000", "trade");
    LedgerItem ledgerItem2 =
            new LedgerItem("tx2", "2018-02-06T16:07:50.122206Z", "ref", "BTC", "-0.5", "trade");
    List<LedgerItem> ledgerItems = new ArrayList<>();
    ledgerItems.add(ledgerItem1);
    ledgerItems.add(ledgerItem2);
    List<UserTrade> userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(ledgerItems);
    assertThat(userTrades).hasSize(1);
    UserTrade userTrade = userTrades.iterator().next();
    assertThat(userTrade.getPrice()).isEqualTo(new BigDecimal("10000"));
  }

  @Test
  public void testAdaptOrderResponseToOrder() {
    OrderResponse orderResponse =
        new OrderResponse(
            "orderId",
            "clientOrderId",
            new BigDecimal("1.2"),
            "buy",
            "BTCUSD.SPOT",
            new BigDecimal("12000"),
            new BigDecimal("11980"),
            Collections.singletonList(
                new OrderResponse.Trade(
                    "BTCUSD.SPOT",
                    "trade1",
                    "rfq1",
                    "2018-02-06T16:07:50.122206Z",
                    new BigDecimal("12000"),
                    new BigDecimal("11980"),
                    "orderId",
                    "buy")),
            "2018-02-06T16:07:50.122206Z");
    Order order = B2C2Adapters.adaptOrderResponseToOrder(orderResponse);
    assertThat(order.getId()).isEqualTo("clientOrderId");
  }
}
