package org.knowm.xchange.b2c2.service;

import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.QuoteRequest;
import org.knowm.xchange.b2c2.dto.trade.QuoteResponse;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

  List<LedgerItem> getLedger(Long offset, Integer limit, String type, Date since)
      throws IOException {
    try {
      String sinceString = null;
      if ( since != null){
        sinceString = ZonedDateTime.ofInstant(since.toInstant(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME);
      }
      return b2c2.ledger(
          this.authorizationHeader,
          offset,
          limit,
          type,
          sinceString);
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
