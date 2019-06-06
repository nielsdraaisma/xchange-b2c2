package org.knowm.xchange.b2c2;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.b2c2.service.B2C2AccountService;
import org.knowm.xchange.b2c2.service.B2C2MarketDataService;
import org.knowm.xchange.b2c2.service.B2C2TradingService;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;

public class B2C2Exchange extends BaseExchange implements Exchange {

  protected B2C2MarketDataService marketDataService;

  @Override
  protected void initServices() {
    this.accountService = new B2C2AccountService(this);
    this.marketDataService = new B2C2MarketDataService(this);
    this.tradeService = new B2C2TradingService(this);
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    return null;
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification exchangeSpecification =
        new ExchangeSpecification(this.getClass().getCanonicalName());
    exchangeSpecification.setSslUri("https://api.b2c2.net/");
    exchangeSpecification.setExchangeName("B2C2");
    exchangeSpecification.setExchangeDescription("B2C2");

    return exchangeSpecification;
  }

  @Override
  public B2C2MarketDataService getMarketDataService() {
    return marketDataService;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {
    this.marketDataService.getInstruments().stream()
        .map(i -> i.name)
        .map(B2C2Adapters::adaptInstrumentToCurrencyPair)
        .forEach(
            currencyPair -> {
              CurrencyPairMetaData metaData =
                  new CurrencyPairMetaData(null, null, null, null, null);
              this.exchangeMetaData.getCurrencyPairs().put(currencyPair, metaData);
            });
  }
}
