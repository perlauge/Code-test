package test.soprasteria.danskebank.account.codetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import test.soprasteria.danskebank.account.codetest.entities.Account;

@Repository
@Transactional
public interface AccountRepository extends CrudRepository<Account, Long> {

    Account findAccountByCustomerId(long customerId);
}
