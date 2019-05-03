package org.knowm.xchange.trade;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Test;
import org.knowm.xchange.b2c2.dto.trade.OrderResponse;
import org.knowm.xchange.b2c2.dto.trade.OrderSide;

public class OrderResponseTest {

  @Test
  public void testUnmarshal() throws IOException {

    InputStream is =
        OrderResponseTest.class.getResourceAsStream(
            "/org/knowm/xchange/b2c2/dto/trade/order-response.json");

    ObjectMapper mapper = new ObjectMapper();
    OrderResponse orderResponse = mapper.readValue(is, OrderResponse.class);

    assertThat(orderResponse.orderId).isEqualTo("d4e41399-e7a1-4576-9b46-349420040e1a");
    assertThat(orderResponse.clientOrderId).isEqualTo("d4e41399-e7a1-4576-9b46-349420040e1a");
    assertThat(orderResponse.quantity).isEqualByComparingTo(new BigDecimal("3"));
    assertThat(orderResponse.side).isEqualTo(OrderSide.BUY.name().toLowerCase());
    assertThat(orderResponse.instrument).isEqualTo("BTCUSD.SPOT");
    assertThat(orderResponse.price).isEqualByComparingTo(new BigDecimal("11000"));
    assertThat(orderResponse.executedPrice).isEqualByComparingTo(new BigDecimal("10457.651100000"));
    assertThat(orderResponse.trades.size()).isEqualByComparingTo(1);
    assertThat(orderResponse.created)
        .isEqualTo(Date.from(ZonedDateTime.parse("2018-02-06T16:07:50.122206Z").toInstant()));
  }
}
