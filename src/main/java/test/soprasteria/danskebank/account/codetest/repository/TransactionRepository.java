package test.soprasteria.danskebank.account.codetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import test.soprasteria.danskebank.account.codetest.entities.Transaction;

import java.util.List;

@Repository
@Transactional
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findFirst10ByAccountIdAndSuccessOrderByTransactionIdDesc(long accountId, boolean success);
}
