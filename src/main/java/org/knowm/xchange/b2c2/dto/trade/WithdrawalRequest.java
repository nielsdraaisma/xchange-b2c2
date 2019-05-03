package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WithdrawalRequest {
  @JsonProperty("amount")
  public final String amount;

  @JsonProperty("currency")
  public final String currency;

  public WithdrawalRequest(String amount, String currency) {
    this.amount = amount;
    this.currency = currency;
  }
}
