package org.knowm.xchange.b2c2.service;

import org.knowm.xchange.b2c2.B2C2;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.exceptions.CurrencyPairNotValidException;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.ExchangeSecurityException;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.RestProxyFactory;

public class B2C2BaseService extends BaseExchangeService<B2C2Exchange> implements BaseService {

  private static final Logger log = LoggerFactory.getLogger(B2C2BaseService.class);
  protected final B2C2 b2c2;

  final String authorizationHeader;

  B2C2BaseService(B2C2Exchange exchange) {
    super(exchange);
    String apiSecret = exchange.getExchangeSpecification().getApiKey();
    if (apiSecret != null) {
      this.authorizationHeader = "Token " + apiSecret;
    } else {
      this.authorizationHeader = null;
    }
    this.b2c2 =
        RestProxyFactory.createProxy(
            B2C2.class, exchange.getExchangeSpecification().getSslUri(), getClientConfig());
  }

  ExchangeException handleException(final B2C2Exception exception) {
    B2C2Exception.Error error = exception.errors.iterator().next();
    if (error.code != null) {
      switch (error.code) {
        case 1100:
          return new ExchangeSecurityException(error.message);
        case 1011:
          return new FundsExceededException();
        case 1001:
          return new CurrencyPairNotValidException();
        default:
          log.warn("No exception mapping for B2C2 exception error code {}", error.code, exception);
          return new ExchangeException(exception);
      }
    } else return new ExchangeException(exception);
  }
}
