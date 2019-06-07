package org.knowm.xchange.b2c2;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Properties;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class B2C2ExchangeIntegration {

  private Exchange exchange;

  @Before
  public void setUp() throws IOException {

    Exchange tmpExchange =
        ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(B2C2Exchange.class);
    ExchangeSpecification exSpec = tmpExchange.getDefaultExchangeSpecification();

    InputStream is =
        B2C2ExchangeIntegration.class.getResourceAsStream(
            "/org/knowm/xchange/b2c2/exchangeConfiguration.json");
    Properties props = new Properties();

    props.load(is);

    exSpec.setSslUri(props.getProperty("api-ssl-uri"));
    exSpec.setApiKey(props.getProperty("api-key"));
    if (props.containsKey("proxy-host")) {
      exSpec.setProxyHost(props.getProperty("proxy-host"));
      exSpec.setProxyPort(Integer.valueOf(props.getProperty("proxy-port")));
    }

    tmpExchange.applySpecification(exSpec);
    exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
  }

  @Test
  public void testCreateAndRetrieveOrder() throws IOException {
    LimitOrder limitOrder =
        new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
            .limitPrice(new BigDecimal("20000"))
            .originalAmount(BigDecimal.ONE)
            .build();
    String orderId = exchange.getTradeService().placeLimitOrder(limitOrder);

    Collection<Order> retrieved = exchange.getTradeService().getOrder(orderId);
    assertThat(retrieved).hasSize(1);
    Order retrievedOrder = retrieved.iterator().next();
    assertThat(retrievedOrder.getId()).isEqualTo(orderId);
    assertThat(retrievedOrder).isInstanceOf(LimitOrder.class);
    LimitOrder retrievedLimitOrder = (LimitOrder) retrievedOrder;
    assertThat(retrievedLimitOrder.getOriginalAmount()).isEqualByComparingTo(BigDecimal.ONE);
    assertThat(retrievedLimitOrder.getLimitPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    assertThat(retrievedLimitOrder.getLimitPrice()).isLessThan(new BigDecimal("20000"));
  }
}
