package org.knowm.xchange.b2c2.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.QuoteRequest;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;

public class B2C2BaseServiceRaw extends B2C2BaseService {

  /** Locking object to ensure a new quite isn't requested whilst a trade is in progress. */
  protected static final Lock quoteLock = new ReentrantLock();

  B2C2BaseServiceRaw(B2C2Exchange exchange) {
    super(exchange);
  }

  QuoteResponse quote(QuoteRequest quoteRequest) throws IOException {
    try {
      return this.b2c2.quote(this.authorizationHeader, quoteRequest);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }

  List<LedgerItem> getLedger() throws IOException {
    try {
      return b2c2.ledger(this.authorizationHeader);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }

  final String toApiInstrument(CurrencyPair currencyPair) {
    try {
      return currencyPair.base.toString() + currencyPair.counter.toString() + ".SPOT";
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }
}
