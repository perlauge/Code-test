package test.soprasteria.danskebank.account.codetest.dto;

import test.soprasteria.danskebank.account.codetest.model.TransactionType;

import java.math.BigDecimal;

public record BalanceUpdateDto(BigDecimal amount, TransactionType transactionType) {
}
