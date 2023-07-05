package test.soprasteria.danskebank.account.codetest.entities;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class TransactionId implements Serializable {
    private long accountId;
    private long transactionId;
}
