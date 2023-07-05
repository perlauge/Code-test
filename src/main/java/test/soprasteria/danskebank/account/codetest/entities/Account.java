package test.soprasteria.danskebank.account.codetest.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

@Entity
public class Account extends RepresentationModel<Account> {

    @Id
    @GeneratedValue
    private long id;

    private long customerId;
    private BigDecimal balance;

    public Account() {
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getId() {
        return this.id;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
