package test.soprasteria.danskebank.account.codetest.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import test.soprasteria.danskebank.account.codetest.assembler.AccountAssembler;
import test.soprasteria.danskebank.account.codetest.dto.AccountCreationDto;
import test.soprasteria.danskebank.account.codetest.dto.BalanceUpdateDto;
import test.soprasteria.danskebank.account.codetest.entities.Account;
import test.soprasteria.danskebank.account.codetest.entities.Transaction;
import test.soprasteria.danskebank.account.codetest.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/")
public class AccountController {

    private final AccountAssembler assembler;
    private final AccountService service;

    public AccountController(AccountAssembler assembler, AccountService service) {
        this.assembler = assembler;
        this.service = service;
    }


    @PostMapping("/accounts")
    public ResponseEntity<?> create(@RequestBody AccountCreationDto accountCreationDto) {
        // TODO: Make sure the requester is allowed to create the account
        Account account = this.service.createAccount(accountCreationDto.customerId());
        if (account == null) {
            account = this.service.getAccountForCustomer(accountCreationDto.customerId());
            account.add(linkTo(methodOn(AccountController.class).show(account.getId())).withSelfRel());
            return ResponseEntity.ok().body(account);
        }
        EntityModel<Account> entityModel = assembler.toModel(account);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/accounts/{accountId}")
    public Account show(@PathVariable long accountId) {
        // TODO: Make sure the requester is allowed to create the account

        Optional<Account> optionalAccount = this.service.getAccount(accountId);
        if (optionalAccount.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("account %d not found", accountId));
        }
        return optionalAccount.get();
    }

    @PostMapping("/accounts/{accountId}")
    public Account update(@PathVariable long accountId, @RequestBody BalanceUpdateDto balanceUpdateDto) {
        // TODO: Make sure the requester is allowed to update the account
        if (BigDecimal.ZERO.compareTo(balanceUpdateDto.amount()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Request with negative amount %s", balanceUpdateDto.amount()));
        }
        Account update = service.update(accountId, balanceUpdateDto);
        if (update == null) {
            if (service.getAccount(accountId).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("account %d not found", accountId));
            }

            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("conflict with account %d", accountId));
        }
        return update;
    }


    @GetMapping("/accounts/{accountId}/transactions")
    public List<Transaction> getTransactions(@PathVariable long accountId) {
        // TODO: Make sure the requester is allowed to view the transactions
        return service.getLatestTransactions(accountId);
    }
}
