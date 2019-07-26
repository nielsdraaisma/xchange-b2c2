package org.knowm.xchange.b2c2.dto.trade;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class OrderRequestTest {

  @Test
  public void testMarshalling() throws JsonProcessingException {
    OrderRequest orderRequest =
        new OrderRequest(
                "uuid",
            "2",
            "buy",
            "BTCUSD.SPOT",
            "12333.23",
            false,
            "2018-02-06T16:07:50.122206Z");

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(orderRequest);
    assertThat(json)
        .isEqualToIgnoringNewLines(
            "{\"client_order_id\":\"uuid\",\"quantity\":\"2\",\"side\":\"buy\",\"instrument\":\"BTCUSD.SPOT\",\"order_type\":\"FOK\",\"price\":\"12333.23\",\"force_open\":false,\"valid_until\":\"2018-02-06T16:07:50.122206Z\"}");
  }
}
