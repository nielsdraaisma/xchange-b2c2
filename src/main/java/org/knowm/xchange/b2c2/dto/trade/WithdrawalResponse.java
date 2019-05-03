package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class WithdrawalResponse {
  public final String amount;
  public final String currency;
  public final UUID withdrawalId;

  public WithdrawalResponse(
      @JsonProperty("amount") final String amount,
      @JsonProperty("currency") final String currency,
      @JsonProperty("withdrawal_id") final UUID withdrawalId) {
    this.amount = amount;
    this.currency = currency;
    this.withdrawalId = withdrawalId;
  }
}
