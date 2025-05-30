/**
 * @author Edinw Zhang
 * @date 2025/05/30
 */
package com.hsbc.wpb.service;

import com.hsbc.wpb.exception.AccountException;
import com.hsbc.wpb.exception.DuplicateTransactionException;
import com.hsbc.wpb.exception.TransactionException;
import com.hsbc.wpb.model.Account;
import com.hsbc.wpb.model.Transaction;
import com.hsbc.wpb.model.TransactionStatus;
import com.hsbc.wpb.model.TransactionType;
import com.hsbc.wpb.repository.InMemoryRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final InMemoryRepository repository;
    private final CacheManager cacheManager;
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("1");

    public TransactionService(InMemoryRepository repository, CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    @CachePut(value = "transactions", key = "#result.id", unless = "#result == null")
    @CacheEvict(value = {"transactionList", "accountTransactions"}, allEntries = true)
    public Transaction createTransaction(Transaction transaction) {
        validateTransaction(transaction);

        // 检查是否存在重复交易
        if (isDuplicateTransaction(transaction)) {
            throw new DuplicateTransactionException("Duplicate transaction detected: " +
                "fromAccount=" + transaction.fromAccount() + 
                ", toAccount=" + transaction.toAccount() + 
                ", amount=" + transaction.amount() + 
                ", type=" + transaction.type());
        }

        try {
            Transaction result = switch (transaction.type()) {
                case DEPOSIT -> processDeposit(transaction);
                case WITHDRAWAL -> processWithdrawal(transaction);
                case TRANSFER -> processTransfer(transaction);
                default -> throw new TransactionException("Unsupported transaction type: " + transaction.type());
            };
            // 同时更新账户缓存
            evictAccountCache(transaction);
            return result;
        } catch (AccountException e) {
            throw new TransactionException("Transaction failed: " + e.getMessage(), e);
        }
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new TransactionException("Transaction cannot be null");
        }
        if (transaction.type() == null) {
            throw new TransactionException("Transaction type cannot be null");
        }
        if (transaction.amount() == null) {
            throw new TransactionException("Transaction amount cannot be null");
        }
        if (transaction.amount().compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new TransactionException("Transaction amount is too small");
        }
        if (transaction.amount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new TransactionException("Transaction amount exceeds maximum limit");
        }
    }

    @CachePut(value = "accounts", key = "#result.accountNumber", unless = "#result == null")
    private Transaction processDeposit(Transaction transaction) {
        if (!repository.validateAccountBalance(transaction.toAccount(), BigDecimal.ZERO)) {
            throw new AccountException("Destination account not found: " + transaction.toAccount());
        }

        try {
            Account account = repository.findAccountByNumber(transaction.toAccount());
            Account updatedAccount = account.withBalance(account.balance().add(transaction.amount()));
            repository.saveAccount(updatedAccount);
            
            Transaction completedTransaction = new Transaction(
                null,
                transaction.fromAccount(),
                transaction.toAccount(),
                transaction.amount(),
                transaction.type(),
                LocalDateTime.now(),
                TransactionStatus.COMPLETED
            );
            return repository.saveTransaction(completedTransaction);
        } catch (Exception e) {
            throw new TransactionException("Failed to process deposit", e);
        }
    }

    @CachePut(value = "accounts", key = "#result.accountNumber", unless = "#result == null")
    private Transaction processWithdrawal(Transaction transaction) {
        if (!repository.validateAccountBalance(transaction.fromAccount(), transaction.amount())) {
            throw new AccountException("Insufficient funds or account not found: " + transaction.fromAccount());
        }

        try {
            Account account = repository.findAccountByNumber(transaction.fromAccount());
            Account updatedAccount = account.withBalance(account.balance().subtract(transaction.amount()));
            repository.saveAccount(updatedAccount);
            
            Transaction completedTransaction = new Transaction(
                null,
                transaction.fromAccount(),
                transaction.toAccount(),
                transaction.amount(),
                transaction.type(),
                LocalDateTime.now(),
                TransactionStatus.COMPLETED
            );
            return repository.saveTransaction(completedTransaction);
        } catch (Exception e) {
            throw new TransactionException("Failed to process withdrawal", e);
        }
    }

    @Caching(put = {
        @CachePut(value = "accounts", key = "#transaction.fromAccount"),
        @CachePut(value = "accounts", key = "#transaction.toAccount")
    })
    private Transaction processTransfer(Transaction transaction) {
        try {
            repository.updateAccountBalances(
                transaction.fromAccount(),
                transaction.toAccount(),
                transaction.amount()
            );
            
            Transaction completedTransaction = new Transaction(
                null,
                transaction.fromAccount(),
                transaction.toAccount(),
                transaction.amount(),
                transaction.type(),
                LocalDateTime.now(),
                TransactionStatus.COMPLETED
            );
            return repository.saveTransaction(completedTransaction);
        } catch (IllegalArgumentException e) {
            throw new AccountException(e.getMessage());
        } catch (Exception e) {
            throw new TransactionException("Failed to process transfer", e);
        }
    }

    @Cacheable(value = "transactionList", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort", unless = "#result.isEmpty()")
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        try {
            List<Transaction> allTransactions = repository.findAllTransactions();
            
            // 获取总数
            long total = allTransactions.size();
            
            // 如果请求的页码超出范围，返回空页
            if (pageable.getPageNumber() * pageable.getPageSize() >= total) {
                return new PageImpl<>(List.of(), pageable, total);
            }
            
            // 计算分页范围
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), (int) total);
            
            // 获取当前页的数据
            List<Transaction> pageContent = allTransactions.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, total);
        } catch (Exception e) {
            throw new TransactionException("Failed to retrieve transactions", e);
        }
    }

    @Cacheable(value = "transactions", key = "#id", unless = "#result == null")
    public Transaction getTransaction(Long id) {
        if (id == null) {
            throw new TransactionException("Transaction ID cannot be null");
        }
        
        Transaction transaction = repository.findTransactionById(id);
        if (transaction == null) {
            throw new TransactionException("Transaction not found with ID: " + id);
        }
        return transaction;
    }

    @Caching(evict = {
        @CacheEvict(value = "transactions", key = "#id"),
        @CacheEvict(value = "transactionList", allEntries = true),
        @CacheEvict(value = "accountTransactions", allEntries = true)
    })
    public void deleteTransaction(Long id) {
        if (id == null) {
            throw new TransactionException("Transaction ID cannot be null");
        }
        
        try {
            Transaction transaction = repository.findTransactionById(id);
            if (transaction == null) {
                throw new TransactionException("Transaction not found with ID: " + id);
            }

            // 检查交易是否可以删除（例如，只允许删除最近24小时内的交易）
            if (transaction.timestamp().isBefore(LocalDateTime.now().minusHours(24))) {
                throw new TransactionException("Cannot delete transactions older than 24 hours");
            }

            repository.deleteTransaction(id);
            // 同时更新账户缓存
            evictAccountCache(transaction);
        } catch (Exception e) {
            throw new TransactionException("Failed to delete transaction", e);
        }
    }

    @Caching(
        put = @CachePut(value = "transactions", key = "#id", unless = "#result == null"),
        evict = {
            @CacheEvict(value = "transactionList", allEntries = true),
            @CacheEvict(value = "accountTransactions", allEntries = true)
        }
    )
    public Transaction modifyTransaction(Long id, Transaction updatedTransaction) {
        if (id == null) {
            throw new TransactionException("Transaction ID cannot be null");
        }
        
        // 验证交易是否存在
        Transaction existingTransaction = repository.findTransactionById(id);
        if (existingTransaction == null) {
            throw new TransactionException("Transaction not found with ID: " + id);
        }

        // 验证新的交易数据
        validateTransaction(updatedTransaction);

        try {
            // 根据交易类型处理修改
            Transaction result = switch (updatedTransaction.type()) {
                case DEPOSIT -> processDeposit(updatedTransaction);
                case WITHDRAWAL -> processWithdrawal(updatedTransaction);
                case TRANSFER -> processTransfer(updatedTransaction);
                default -> throw new TransactionException("Unsupported transaction type: " + updatedTransaction.type());
            };

            // 更新交易ID和时间戳
            Transaction modifiedTransaction = new Transaction(
                id,
                result.fromAccount(),
                result.toAccount(),
                result.amount(),
                result.type(),
                LocalDateTime.now(),
                TransactionStatus.COMPLETED
            );

            // 保存修改后的交易
            Transaction savedTransaction = repository.saveTransaction(modifiedTransaction);
            
            // 更新相关账户的缓存
            evictAccountCache(savedTransaction);
            
            return savedTransaction;
        } catch (AccountException e) {
            throw new TransactionException("Transaction modification failed: " + e.getMessage(), e);
        }
    }

    @Cacheable(value = "transactionSearch", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort + '-' + #fromAccount + '-' + #toAccount + '-' + #type + '-' + #minAmount + '-' + #maxAmount + '-' + #startDate + '-' + #endDate", unless = "#result.isEmpty()")
    public Page<Transaction> searchTransactions(
        String fromAccount,
        String toAccount,
        TransactionType type,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        try {
            List<Transaction> allTransactions = repository.findAllTransactions();
            
            // 应用过滤条件
            List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> fromAccount == null || t.fromAccount().equals(fromAccount))
                .filter(t -> toAccount == null || t.toAccount().equals(toAccount))
                .filter(t -> type == null || t.type() == type)
                .filter(t -> minAmount == null || t.amount().compareTo(minAmount) >= 0)
                .filter(t -> maxAmount == null || t.amount().compareTo(maxAmount) <= 0)
                .filter(t -> startDate == null || !t.timestamp().isBefore(startDate))
                .filter(t -> endDate == null || !t.timestamp().isAfter(endDate))
                .collect(Collectors.toList());

            // 获取过滤后的总数
            long total = filteredTransactions.size();
            
            // 如果请求的页码超出范围，返回空页
            if (pageable.getPageNumber() * pageable.getPageSize() >= total) {
                return new PageImpl<>(List.of(), pageable, total);
            }
            
            // 计算分页范围
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), (int) total);
            
            // 获取当前页的数据
            List<Transaction> pageContent = filteredTransactions.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, total);
        } catch (Exception e) {
            throw new TransactionException("Failed to search transactions", e);
        }
    }

    // 辅助方法：清除账户相关缓存
    @Caching(evict = {
        @CacheEvict(value = "accounts", key = "#transaction.fromAccount", condition = "#transaction.fromAccount != null"),
        @CacheEvict(value = "accounts", key = "#transaction.toAccount", condition = "#transaction.toAccount != null"),
        @CacheEvict(value = "accountTransactions", key = "#transaction.fromAccount", condition = "#transaction.fromAccount != null"),
        @CacheEvict(value = "accountTransactions", key = "#transaction.toAccount", condition = "#transaction.toAccount != null")
    })
    private void evictAccountCache(Transaction transaction) {
        // 缓存清除逻辑已通过注解实现
        // 该方法现在作为一个标记方法，实际的缓存清除由Spring的缓存机制处理
    }

    // 检查是否存在重复交易
    private boolean isDuplicateTransaction(Transaction transaction) {
        List<Transaction> recentTransactions = repository.findAllTransactions().stream()
            .filter(t -> t.fromAccount().equals(transaction.fromAccount()))
            .filter(t -> t.toAccount().equals(transaction.toAccount()))
            .filter(t -> t.amount().equals(transaction.amount()))
            .filter(t -> t.type() == transaction.type())
            .filter(t -> t.timestamp().isAfter(LocalDateTime.now().minusMinutes(5))) // 5分钟内的交易
            .collect(Collectors.toList());
        
        return !recentTransactions.isEmpty();
    }
}
