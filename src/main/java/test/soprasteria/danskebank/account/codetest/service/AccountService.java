package test.soprasteria.danskebank.account.codetest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import test.soprasteria.danskebank.account.codetest.dto.BalanceUpdateDto;
import test.soprasteria.danskebank.account.codetest.entities.Account;
import test.soprasteria.danskebank.account.codetest.entities.Transaction;
import test.soprasteria.danskebank.account.codetest.model.TransactionType;
import test.soprasteria.danskebank.account.codetest.repository.AccountRepository;
import test.soprasteria.danskebank.account.codetest.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account createAccount(long customerId) {
        Account account = accountRepository.findAccountByCustomerId(customerId);
        if (account == null) {
            account = new Account();
            account.setCustomerId(customerId);
            account.setBalance(BigDecimal.ZERO);
            account = accountRepository.save(account);
            return account;
        } // should we return a fault when trying to create another account for the same customer?
        return null;

    }

    public Account getAccountForCustomer(long customerId) {
        return accountRepository.findAccountByCustomerId(customerId);
    }

    public Optional<Account> getAccount(long accountId) {
        return accountRepository.findById(accountId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Account update(long accountId, BalanceUpdateDto balanceUpdateDto) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            Transaction transaction = new Transaction();
            transaction.setAmount(balanceUpdateDto.amount());
            transaction.setAccountId(account.getId());
            transaction.setTransactionType(balanceUpdateDto.transactionType());
            transaction.setTimeOfTransaction(OffsetDateTime.now());
            transaction.setSuccess(true);
            transaction.setNewBalance(account.getBalance());
            if (balanceUpdateDto.transactionType() == TransactionType.DEPOSIT) {
                account.setBalance(account.getBalance().add(balanceUpdateDto.amount()));
            } else {
                // Withdrawal
                if (account.getBalance().compareTo(balanceUpdateDto.amount()) >= 0) {
                    // Sufficient funds
                    account.setBalance(account.getBalance().subtract(balanceUpdateDto.amount()));
                } else {
                    // insufficient funds - we'll save the failed transaction request. It could be useful in an audit.
                    transaction.setSuccess(false);
                    transactionRepository.save(transaction);
                    return null; // Maybe throw a custom exception: insufficient funds
                }
            }
            transaction.setNewBalance(account.getBalance());
            account = accountRepository.save(account); // save the account within the transaction scope.
            transactionRepository.save(transaction);
            return account;
        }
        return null; // maybe throw custom exception: no such account
    }

    public List<Transaction> getLatestTransactions(long accountId) {
        List<Transaction> first10ByAccountIdOOrderByAuditIdDesc = transactionRepository.findFirst10ByAccountIdAndSuccessOrderByTransactionIdDesc(accountId, true);
        return first10ByAccountIdOOrderByAuditIdDesc;
    }
}
