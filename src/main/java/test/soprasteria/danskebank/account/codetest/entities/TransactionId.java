package test.soprasteria.danskebank.account.codetest.entities;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class TransactionId implements Serializable {
    private static final long serialVersionUID = 1L;
    private long accountId;
    private long transactionId;

    public TransactionId() {
    }
}
