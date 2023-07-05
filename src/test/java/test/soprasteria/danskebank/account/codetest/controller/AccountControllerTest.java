package test.soprasteria.danskebank.account.codetest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import test.soprasteria.danskebank.account.codetest.dto.AccountCreationDto;
import test.soprasteria.danskebank.account.codetest.dto.BalanceUpdateDto;
import test.soprasteria.danskebank.account.codetest.model.TransactionType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class AccountControllerTest {

    private static final long CUSTOMER_ID = 123456789L;
    private static AccountCreationDto creationDto = new AccountCreationDto(CUSTOMER_ID);

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() throws Exception {
        createAnInitialAccount();
    }

    @Test
    void createAccount() {
        // it should be possible to create an account for a customer by providing the customerId
        // this is now handled in the beforeEach execution of createAnInitialAccount
    }

    @Test
    void createSameAccountAgain() throws Exception {

        // but the second time around, it should just return the account
        this.mockMvc.perform(post("/accounts").content(asJsonString(creationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(CUSTOMER_ID))
                .andDo(document("home"));

    }


    @Test
    void fetchTheAccountToReadTheBalance() throws Exception {

        // it should be possible to fetch the account for the customer by providing the accountId
        this.mockMvc.perform(get("/accounts/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(CUSTOMER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("0.0"))
                .andDo(document("show-balance"));
    }

    @Test
    void fetchingAnAccountThatDoesNotExistIs404() throws Exception {
        // it should be possible to fetch the account for the customer by providing the accountId
        this.mockMvc.perform(get("/accounts/192837")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("home"));
    }

    @Test
    void accountIdsAreNumeric() throws Exception {
        // it should be possible to fetch the account for the customer by providing the accountId
        this.mockMvc.perform(get("/accounts/my-account")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void depositingIsReflectedOnTheBalance() throws Exception {
        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(10, 0), TransactionType.DEPOSIT);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("10.0"))
                .andDo(document("deposit"));
    }

    @Test
    void depositingToAnUnknownAccountFails() throws Exception {
        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(10, 0), TransactionType.DEPOSIT);
        this.mockMvc.perform(post("/accounts/6435").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(document("deposit-fail"));
    }
    @ParameterizedTest
    @CsvSource(value = {"0:400", "-1:400"}, delimiter = ':')
    void depositingNonPositiveAmountsIsProhibited(String amount, int expected) throws Exception {

        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(new BigDecimal(amount), TransactionType.DEPOSIT);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expected))
                .andDo(document("deposit-fail2"));
    }

    @ParameterizedTest
    @CsvSource(value = {"0:400", "-1:400"}, delimiter = ':')
    void withdrawingNonPositiveAmountsIsProhibited(String amount, int expected) throws Exception {

        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(new BigDecimal(amount), TransactionType.WITHDRAW);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expected))
                .andDo(document("withdraw"));
    }

    @Test
    void withdrawingLessThanOrEqualToAvailbleIsReflectedInTheBalance() throws Exception {
        // setup account with a balance of 10
        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(10, 0), TransactionType.DEPOSIT);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("10.0"));

        balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(10, 0), TransactionType.WITHDRAW);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("0.0"))
                .andDo(document("withdraw"));

    }
    @Test
    void withdrawingMoreThanAvailbleIsProhibited() throws Exception {
        BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(10, 0), TransactionType.DEPOSIT);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("10.0"));

        balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(101, 1), TransactionType.WITHDRAW);
        this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(document("withdraw-fail"));
    }


    @Test
    void listingTransactionsProvidesTheLatest10TransactionsButNoneForANewAccount() throws Exception {
        this.mockMvc.perform(get("/accounts/1/transactions")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(0)));
    }
    @Test
    void listingTransactionsProvidesTheLatest10Transactions() throws Exception {
        for (int i = 1; i <= 15; i++) {
            BalanceUpdateDto balanceUpdateDto = new BalanceUpdateDto(BigDecimal.valueOf(i, 0), TransactionType.DEPOSIT);
            this.mockMvc.perform(post("/accounts/1").content(asJsonString(balanceUpdateDto))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        this.mockMvc.perform(get("/accounts/1/transactions")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(10)))
                .andDo(document("transactions"));
    }


    private void createAnInitialAccount() throws Exception {
        this.mockMvc.perform(post("/accounts").content(asJsonString(creationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(CUSTOMER_ID))
                .andDo(document("home"));
    }


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
