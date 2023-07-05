package test.soprasteria.danskebank.account.codetest.entities;


import jakarta.persistence.*;
import test.soprasteria.danskebank.account.codetest.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@IdClass(TransactionId.class)
public class Transaction {

    @Id
    private long accountId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long transactionId;

    private OffsetDateTime timeOfTransaction;

    private TransactionType transactionType;

    private BigDecimal amount;

    private BigDecimal newBalance;

    private boolean success;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public OffsetDateTime getTimeOfTransaction() {
        return timeOfTransaction;
    }

    public void setTimeOfTransaction(OffsetDateTime timeOfTransaction) {
        this.timeOfTransaction = timeOfTransaction;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
