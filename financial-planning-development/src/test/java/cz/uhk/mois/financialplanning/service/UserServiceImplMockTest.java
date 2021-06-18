package cz.uhk.mois.financialplanning.service;

import cz.uhk.mois.financialplanning.bank.DaySupport;
import cz.uhk.mois.financialplanning.bank.DaySupportImpl;
import cz.uhk.mois.financialplanning.configuration.BankSupport;
import cz.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoOut;
import cz.uhk.mois.financialplanning.model.dto.user.UserDtoOut;
import cz.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import cz.uhk.mois.financialplanning.model.entity.user.Role;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.repository.TransactionRepository;
import cz.uhk.mois.financialplanning.repository.UserRepository;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.bank.BankTransactionService;
import cz.uhk.mois.financialplanning.service.bank.BankTransactionServiceImpl;
import cz.uhk.mois.financialplanning.service.bank.UniTransaction;
import cz.uhk.mois.financialplanning.service.support.UserSupport;
import cz.uhk.mois.financialplanning.service.support.UserSupportImpl;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.CITY;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.EMAIL;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.FIRST_NAME;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.LAST_NAME;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.PSC;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.STREET;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.TELEPHONE_NUMBER;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.createCreateUserDtoIn;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUpdateUserDtoIn;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jan Krunčík
 * @since 24.04.2020 0:00
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserServiceImplMockTest.UserServiceImplMockTestConfig.class)
@Log4j2
public class UserServiceImplMockTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private ModelMapper modelMapper;

    @Captor
    private ArgumentCaptor<List<Transaction>> transactionListArgumentCaptor;

    @Test
    void add_Hds() {
        log.info("Test adding a user to the database. The account number will be entered. Accordingly, the transactions are read from the bank database and copied to ours.");

        // Mock preparation
        clearMocks();

        when(bankTransactionService.getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), anyLong()))
                .thenReturn(Mono.just(BankSupport.createDummyBankTransactionDtoOutList(ACCOUNT_ID)));

        User user = createUser(false);
        user.setId(1L);
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Data preparation
        CreateUserDtoIn dtoIn = createCreateUserDtoIn();

        // Execution
        Either<Failure, Success<UserDtoOut>> add = userService.add(dtoIn);

        // Verification
        assertTrue(add.isRight());

        assertEquals(HttpStatus.CREATED, add.get().getHttpStatus());

        UserDtoOut userDtoOut = add.get().getBody();
        assertEquals(FIRST_NAME, userDtoOut.getFirstName());
        assertEquals(LAST_NAME, userDtoOut.getLastName());
        assertEquals(EMAIL, userDtoOut.getEmail());
        assertEquals(ACCOUNT_ID, userDtoOut.getAccountId());
        assertEquals(TELEPHONE_NUMBER, userDtoOut.getTelephoneNumber());
        assertEquals(STREET, userDtoOut.getAddress().getStreet());
        assertEquals(CITY, userDtoOut.getAddress().getCity());
        assertEquals(PSC, userDtoOut.getAddress().getPsc());
        assertThat(userDtoOut.getRoles(), hasSize(1));
        assertThat(userDtoOut.getRoles(), contains(Role.USER));

        verify(transactionRepository, times(1))
                .saveAll(transactionListArgumentCaptor.capture());
        List<List<Transaction>> allValues = transactionListArgumentCaptor.getAllValues();

        assertNotNull(allValues);
        assertThat(allValues, hasSize(1));

        List<Transaction> transactionList = allValues.get(0);
        assertNotNull(transactionList);
        assertThat(transactionList, hasSize(2));

        transactionList.parallelStream()
                       .forEach(transaction -> assertEquals(ACCOUNT_ID, transaction.getAccountId()));
    }

    @Test
    void update_AccountId_FromNullToNull() {
        log.info("User (profile) data editing test. The original and new account numbers will be null, so there will be no transactions in the DB, nor will any be deleted.");

        // Data preparation
        User user = createUser();
        user.setAccountId(null);
        Long id = 1L;
        user.setId(id);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        dtoIn.setAccountId(null);

        // Mock preparation
        clearMocks();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionRepository, times(0)).deleteAllByAccountId(anyLong());
        verify(bankTransactionService, times(0)).getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), anyLong());
        verify(transactionRepository, times(0)).saveAll(anyIterable());
    }

    @Test
    void update_AccountId_WithoutChange() {
        log.info("User (profile) data editing test. The original and the new account number will be the same, so there will be some transactions of the respective user in the DB, but they will not be deleted or new ones will be copied.");

        // Data preparation
        User user = createUser();
        Long id = 1L;
        user.setId(id);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();

        // Mock preparation
        clearMocks();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionRepository, times(0)).deleteAllByAccountId(anyLong());
        verify(bankTransactionService, times(0)).getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), anyLong());
        verify(transactionRepository, times(0)).saveAll(anyIterable());
    }

    @Test
    void update_AccountId_Change() {
        log.info("User (profile) data editing test. The user changes the existing account number. So the original transactions are deleted and the new transactions related to the new account number are copied.");

        // Data preparation
        User user = createUser();
        Long id = 1L;
        user.setId(id);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        Long newAccountId = 2L;
        dtoIn.setAccountId(newAccountId);

        // Mock preparation
        clearMocks();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        List<UniTransaction> dummyBankTransactionDtoOutList = BankSupport.createDummyBankTransactionDtoOutList(newAccountId);
        when(bankTransactionService.getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), eq(newAccountId)))
                .thenReturn(Mono.just(dummyBankTransactionDtoOutList));

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionRepository, times(1)).deleteAllByAccountId(eq(ACCOUNT_ID));
        verify(bankTransactionService, times(1)).getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), eq(newAccountId));

        verify(transactionRepository, times(1)).saveAll(transactionListArgumentCaptor.capture());
        List<Transaction> savedTransactionList = transactionListArgumentCaptor.getValue();
        assertThat(savedTransactionList, hasSize(2));
        assertEquals(savedTransactionList.get(0), modelMapper.map(dummyBankTransactionDtoOutList.get(0), Transaction.class));
        assertEquals(savedTransactionList.get(1), modelMapper.map(dummyBankTransactionDtoOutList.get(1), Transaction.class));
    }

    @Test
    void update_AccountId_FromNullToNumber() {
        log.info("User (profile) data editing test. The original account number is entered, so there will be some transactions of the respective user in the database. But as an update, the user removes the account number, so there will be no transactions in the database. The original ones will be deleted.");

        // Data preparation
        User user = createUser();
        user.setAccountId(null);
        Long id = 1L;
        user.setId(id);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        Long newAccountId = ACCOUNT_ID;
        dtoIn.setAccountId(newAccountId);

        // Mock preparation
        clearMocks();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        List<UniTransaction> dummyBankTransactionDtoOutList = BankSupport.createDummyBankTransactionDtoOutList(newAccountId);
        when(bankTransactionService.getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), eq(newAccountId)))
                .thenReturn(Mono.just(dummyBankTransactionDtoOutList));

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionRepository, times(0)).deleteAllByAccountId(eq(null));
        verify(bankTransactionService, times(1)).getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), eq(newAccountId));

        verify(transactionRepository, times(1)).saveAll(transactionListArgumentCaptor.capture());
        List<Transaction> savedTransactionList = transactionListArgumentCaptor.getValue();
        assertThat(savedTransactionList, hasSize(2));
        assertEquals(savedTransactionList.get(0), modelMapper.map(dummyBankTransactionDtoOutList.get(0), Transaction.class));
        assertEquals(savedTransactionList.get(1), modelMapper.map(dummyBankTransactionDtoOutList.get(1), Transaction.class));
    }

    @Test
    void update_AccountId_FromNumberToNull() {
        log.info("User (profile) data editing test. The user sets a new account number, but the original one will not be entered. So there will be no transactions in the database and only the transactions related to the newly entered account number will be copied.");

        // Data preparation
        User user = createUser();
        Long id = 1L;
        user.setId(id);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        Long newAccountId = null;
        dtoIn.setAccountId(newAccountId);

        // Mock preparation
        clearMocks();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionRepository, times(1)).deleteAllByAccountId(eq(ACCOUNT_ID));
        verify(transactionRepository, times(0)).saveAll(anyIterable());
        verify(bankTransactionService, times(0)).getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), eq(newAccountId));
    }

    private void clearMocks() {
        Mockito.clearInvocations(userRepository);
        Mockito.clearInvocations(bankTransactionService);
        Mockito.clearInvocations(transactionRepository);

        Mockito.reset(userRepository);
        Mockito.reset(bankTransactionService);
        Mockito.reset(transactionRepository);
    }

    @Configuration
    static class UserServiceImplMockTestConfig {

        @Bean
        BankTransactionService bankTransactionService() {
            return mock(BankTransactionServiceImpl.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        TransactionRepository transactionRepository() {
            return mock(TransactionRepository.class);
        }

        @Bean
        ModelMapper modelMapper() {
            return new ModelMapper();
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        DaySupport daySupport() {
            return new DaySupportImpl();
        }

        @Bean
        CopyTransactionService copyTransactionService() {
            return new CopyTransactionServiceImpl(transactionRepository(), bankTransactionService(), modelMapper(), daySupport(), clock());
        }

        @Bean
        UserSupport userSupport() {
            return new UserSupportImpl(userRepository());
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        UserService userService() {
            return new UserServiceImpl(userRepository(), copyTransactionService(), transactionRepository(), passwordEncoder(), modelMapper(), userSupport(), clock());
        }
    }

}
