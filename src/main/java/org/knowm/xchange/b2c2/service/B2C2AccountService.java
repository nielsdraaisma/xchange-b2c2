package org.knowm.xchange.b2c2.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.b2c2.B2C2Adapters;
import org.knowm.xchange.b2c2.B2C2Exchange;
import org.knowm.xchange.b2c2.dto.trade.LedgerItem;
import org.knowm.xchange.b2c2.dto.trade.WithdrawalRequest;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.*;

public class B2C2AccountService extends B2C2AccountServiceRaw implements AccountService {
  public B2C2AccountService(B2C2Exchange exchange) {
    super(exchange);
  }

  @Override
  public String withdrawFunds(Currency currency, BigDecimal amount, String address)
      throws IOException {
    return super.withdraw(new WithdrawalRequest(amount.toPlainString(), currency.toString()))
        .withdrawalId
        .toString();
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    List<Wallet> wallets = new ArrayList<>();
    for (Map.Entry<String, String> entry : getAccountBalances().entrySet()) {
      wallets.add(
          new Wallet(
              entry.getKey(),
              new Balance(new Currency(entry.getKey()), new BigDecimal(entry.getValue()))));
    }
    return new AccountInfo(wallets);
  }

  @Override
  public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
    List<FundingRecord> matching = new ArrayList<>();
    if (!(params instanceof B2C2FundingHistoryParams)) {
      throw new IllegalArgumentException("Invalid params given");
    }
    B2C2FundingHistoryParams b2C2FundingHistoryParams = (B2C2FundingHistoryParams) params;
    for (LedgerItem item :
        getLedger(
            b2C2FundingHistoryParams.offset,
            b2C2FundingHistoryParams.limit,
            null,
            b2C2FundingHistoryParams.since)) {
      if (!item.type.equals("transfer")) {
        break;
      }
      // Filter by currency
      if (b2C2FundingHistoryParams.getCurrency() != null
          && !item.currency.equals(b2C2FundingHistoryParams.getCurrency().toString())) {
        break;
      }
      // Filter by id
      if (!item.transactionId.equals(b2C2FundingHistoryParams.getTransactionId())) {
        break;
      }
      matching.add(B2C2Adapters.adaptLedgerItemToFundingRecord(item));
    }
    return matching;
  }

  @Override
  public TradeHistoryParams createFundingHistoryParams() {
    return new B2C2FundingHistoryParams();
  }

  static class B2C2FundingHistoryParams
      implements TradeHistoryParamCurrency, TradeHistoryParamTransactionId, TradeHistoryParamsTimeSpan, TradeHistoryParamOffset, TradeHistoryParamLimit {
    private Currency currency;
    private String transactionId;
    private Date since;
    private Long offset;
    private Integer limit;

    @Override
    public Currency getCurrency() {
      return currency;
    }

    @Override
    public void setCurrency(Currency currency) {
      this.currency = currency;
    }

    @Override
    public String getTransactionId() {
      return transactionId;
    }

    @Override
    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    @Override
    public Date getStartTime() {
      return since;
    }

    @Override
    public void setStartTime(Date startTime) {
      this.since = startTime;
    }

    @Override
    public Date getEndTime() {
      return null;
    }

    @Override
    public void setEndTime(Date endTime) {
      throw new NotYetImplementedForExchangeException();
    }

    @Override
    public Long getOffset() {
      return offset;
    }

    @Override
    public void setOffset(Long offset) {
      this.offset = offset;
    }

    @Override
    public Integer getLimit() {
      return limit;
    }

    @Override
    public void setLimit(Integer limit) {
      this.limit = limit;
    }
  }
}
