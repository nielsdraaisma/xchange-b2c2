package org.knowm.xchange.b2c2.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LedgerItem {
  public final String transactionId;
  public final String created;
  public final String reference;
  public final String currency;
  public final String amount;
  public final String type;

  public LedgerItem(
      @JsonProperty("transaction_id") String transactionId,
      @JsonProperty("created") String created,
      @JsonProperty("reference") String reference,
      @JsonProperty("currency") String currency,
      @JsonProperty("amount") String amount,
      @JsonProperty("type") String type) {
    this.transactionId = transactionId;
    this.created = created;
    this.reference = reference;
    this.currency = currency;
    this.amount = amount;
    this.type = type;
  }
}
