package com.hsbc.wpb.service;

import com.hsbc.wpb.exception.AccountException;
import com.hsbc.wpb.exception.TransactionException;
import com.hsbc.wpb.model.Account;
import com.hsbc.wpb.model.AccountType;
import com.hsbc.wpb.model.Transaction;
import com.hsbc.wpb.model.TransactionStatus;
import com.hsbc.wpb.model.TransactionType;
import com.hsbc.wpb.repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private InMemoryRepository repository;

    private TransactionService transactionService;
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("transactions", "accounts", "transactionList", "accountTransactions");
        transactionService = new TransactionService(repository, cacheManager);
    }

    @Test
    void createTransaction_Deposit_Success() {
        // Arrange
        Transaction transaction = Transaction.create(null, "123", new BigDecimal("100.00"), TransactionType.DEPOSIT);
        Account account = Account.create("123", "John Doe", new BigDecimal("500.00"), AccountType.SAVINGS);
        
        when(repository.validateAccountBalance("123", BigDecimal.ZERO)).thenReturn(true);
        when(repository.findAccountByNumber("123")).thenReturn(account);
        when(repository.saveAccount(any())).thenReturn(account);
        when(repository.saveTransaction(any())).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.type());
        assertEquals(TransactionStatus.PENDING, result.status());
        verify(repository).saveAccount(any());
        verify(repository).saveTransaction(any());
    }

    @Test
    void createTransaction_Withdrawal_Success() {
        // Arrange
        Transaction transaction = Transaction.create("123", null, new BigDecimal("100.00"), TransactionType.WITHDRAWAL);
        Account account = Account.create("123", "John Doe", new BigDecimal("500.00"), AccountType.SAVINGS);
        
        when(repository.validateAccountBalance("123", new BigDecimal("100.00"))).thenReturn(true);
        when(repository.findAccountByNumber("123")).thenReturn(account);
        when(repository.saveAccount(any())).thenReturn(account);
        when(repository.saveTransaction(any())).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.type());
        verify(repository).saveAccount(any());
        verify(repository).saveTransaction(any());
    }

    @Test
    void createTransaction_Transfer_Success() {
        // Arrange
        Transaction transaction = Transaction.create("123", "456", new BigDecimal("100.00"), TransactionType.TRANSFER);
        
        doNothing().when(repository).updateAccountBalances("123", "456", new BigDecimal("100.00"));
        when(repository.saveTransaction(any())).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.type());
        verify(repository).updateAccountBalances("123", "456", new BigDecimal("100.00"));
        verify(repository).saveTransaction(any());
    }

    @Test
    void createTransaction_InvalidAmount() {
        // Arrange
        Transaction transaction = Transaction.create("123", "456", new BigDecimal("0.00"), TransactionType.TRANSFER);

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.createTransaction(transaction));
    }

    @Test
    void createTransaction_AccountNotFound() {
        // Arrange
        Transaction transaction = Transaction.create("123", "456", new BigDecimal("100.00"), TransactionType.TRANSFER);
        
        doThrow(new AccountException("Account not found"))
            .when(repository).updateAccountBalances("123", "456", new BigDecimal("100.00"));

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.createTransaction(transaction));
    }

    @Test
    void getTransaction_Success() {
        // Arrange
        Long transactionId = 1L;
        Transaction expectedTransaction = Transaction.create("123", "456", new BigDecimal("100.00"), TransactionType.TRANSFER);
        
        when(repository.findTransactionById(transactionId)).thenReturn(expectedTransaction);

        // Act
        Transaction result = transactionService.getTransaction(transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.type());
    }

    @Test
    void getTransaction_NotFound() {
        // Arrange
        Long transactionId = 999L;
        when(repository.findTransactionById(transactionId)).thenReturn(null);

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.getTransaction(transactionId));
    }

    @Test
    void getAllTransactions_Success() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(
            Transaction.create("123", "456", new BigDecimal("100.00"), TransactionType.TRANSFER),
            Transaction.create("789", "012", new BigDecimal("200.00"), TransactionType.DEPOSIT)
        );
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Transaction> expectedPage = new PageImpl<>(expectedTransactions, pageRequest, expectedTransactions.size());
        
        when(repository.findAllTransactions()).thenReturn(expectedTransactions);

        // Act
        Page<Transaction> result = transactionService.getAllTransactions(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getNumber());
        assertEquals(TransactionType.TRANSFER, result.getContent().get(0).type());
        assertEquals(TransactionType.DEPOSIT, result.getContent().get(1).type());
    }

    @Test
    void deleteTransaction_Success() {
        // Arrange
        Long transactionId = 1L;
        Transaction transaction = Transaction.create("123", "456", new BigDecimal("100.00"), TransactionType.TRANSFER);
        
        when(repository.findTransactionById(transactionId)).thenReturn(transaction);
        doNothing().when(repository).deleteTransaction(transactionId);

        // Act & Assert
        assertDoesNotThrow(() -> transactionService.deleteTransaction(transactionId));
        verify(repository).deleteTransaction(transactionId);
    }

    @Test
    void deleteTransaction_NotFound() {
        // Arrange
        Long transactionId = 999L;
        when(repository.findTransactionById(transactionId)).thenReturn(null);

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.deleteTransaction(transactionId));
    }

    @Test
    void validateTransaction_NullTransaction() {
        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.createTransaction(null));
    }

    @Test
    void validateTransaction_NullType() {
        // Arrange
        Transaction transaction = Transaction.create("123", "456", new BigDecimal("100.00"), null);

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.createTransaction(transaction));
    }

    @Test
    void validateTransaction_NullAmount() {
        // Arrange
        Transaction transaction = Transaction.create("123", "456", null, TransactionType.TRANSFER);

        // Act & Assert
        assertThrows(TransactionException.class, () -> 
            transactionService.createTransaction(transaction));
    }
} 