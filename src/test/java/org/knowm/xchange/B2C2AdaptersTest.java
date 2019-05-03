package org.knowm.xchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.knowm.xchange.b2c2.B2C2Adapters;
import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.UserTrade;

public class B2C2AdaptersTest {

  @Test
  public void testAdaptInstrumentToCurrencyPair() {
    assertEquals(CurrencyPair.BTC_USD, B2C2Adapters.adaptInstrumentToCurrencyPair("BTCUSD.SPOT"));
    assertEquals(CurrencyPair.BTC_USD, B2C2Adapters.adaptInstrumentToCurrencyPair("BTCUSD.CFD"));
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
    assertNotNull(ticker);
    assertEquals(ticker.getCurrencyPair(), CurrencyPair.BTC_AUD);
    assertEquals(ticker.getLast(), new BigDecimal("700.00000000"));
    assertEquals(
        ticker.getTimestamp(),
        Date.from(ZonedDateTime.parse("2018-02-06T16:07:50.122206Z").toInstant()));
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
    assertEquals(userTrades.size(), 1);
    UserTrade userTrade = userTrades.iterator().next();
    assertEquals(userTrade.getOriginalAmount(), new BigDecimal("1"));
    assertEquals(userTrade.getPrice(), new BigDecimal("6000"));
    assertEquals(userTrade.getType(), Order.OrderType.BID);
    assertEquals(userTrade.getCurrencyPair(), CurrencyPair.BTC_USD);

    //     Change amount to -1, should now become a ask
    ledgerItem1 =
        new LedgerItem("tx1", "2018-02-06T16:07:50.122206Z", "ref", "USD", "6000", "trade");
    ledgerItem2 = new LedgerItem("tx2", "2018-02-06T16:07:50.122206Z", "ref", "BTC", "-1", "trade");
    ledgerItems.clear();
    ledgerItems.add(ledgerItem1);
    ledgerItems.add(ledgerItem2);

    userTrades = B2C2Adapters.adaptLedgerItemToUserTrades(ledgerItems);
    userTrade = userTrades.iterator().next();
    assertEquals(userTrade.getOriginalAmount(), new BigDecimal("1"));
    assertEquals(userTrade.getType(), Order.OrderType.ASK);
    assertEquals(userTrade.getCurrencyPair(), CurrencyPair.BTC_USD);
  }
}
