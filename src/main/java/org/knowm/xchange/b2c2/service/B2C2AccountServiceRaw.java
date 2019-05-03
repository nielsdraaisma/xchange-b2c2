package org.knowm.xchange.b2c2.service;

import java.io.IOException;
import java.util.Map;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.WithdrawalRequest;
import org.knowm.xchange.b2c2.dto.trade.WithdrawalResponse;

public class B2C2AccountServiceRaw extends B2C2BaseServiceRaw {

  B2C2AccountServiceRaw(B2C2Exchange exchange) {
    super(exchange);
  }

  Map<String, String> getAccountBalances() throws IOException {
    try {
      return b2c2.getBalances(this.authorizationHeader);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }

  WithdrawalResponse withdraw(WithdrawalRequest withdrawalRequest) throws IOException {
    try {
      return b2c2.withdraw(this.authorizationHeader, withdrawalRequest);
    } catch (B2C2Exception e) {
      throw handleException(e);
    }
  }
}
