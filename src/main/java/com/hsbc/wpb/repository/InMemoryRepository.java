/**
 * @author Edinw Zhang
 * @date 2025/05/30
 */
package com.hsbc.wpb.repository;

import com.hsbc.wpb.model.Transaction;
import com.hsbc.wpb.model.Account;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;
import java.math.BigDecimal;
import jakarta.annotation.PostConstruct;
import com.hsbc.wpb.model.AccountType;

@Repository
public class InMemoryRepository {
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);
    private final ReentrantReadWriteLock accountLock = new ReentrantReadWriteLock();

    @PostConstruct
    public void initializeTestData() {
        // 创建50个测试账户，增加初始余额
        Account account1 = Account.create("ACC001", "Test Account 1", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account2 = Account.create("ACC002", "Test Account 2", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account3 = Account.create("ACC003", "Test Account 3", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account4 = Account.create("ACC004", "Test Account 4", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account5 = Account.create("ACC005", "Test Account 5", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account6 = Account.create("ACC006", "Test Account 6", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account7 = Account.create("ACC007", "Test Account 7", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account8 = Account.create("ACC008", "Test Account 8", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account9 = Account.create("ACC009", "Test Account 9", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account10 = Account.create("ACC010", "Test Account 10", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account11 = Account.create("ACC011", "Test Account 11", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account12 = Account.create("ACC012", "Test Account 12", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account13 = Account.create("ACC013", "Test Account 13", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account14 = Account.create("ACC014", "Test Account 14", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account15 = Account.create("ACC015", "Test Account 15", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account16 = Account.create("ACC016", "Test Account 16", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account17 = Account.create("ACC017", "Test Account 17", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account18 = Account.create("ACC018", "Test Account 18", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account19 = Account.create("ACC019", "Test Account 19", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account20 = Account.create("ACC020", "Test Account 20", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account21 = Account.create("ACC021", "Test Account 21", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account22 = Account.create("ACC022", "Test Account 22", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account23 = Account.create("ACC023", "Test Account 23", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account24 = Account.create("ACC024", "Test Account 24", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account25 = Account.create("ACC025", "Test Account 25", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account26 = Account.create("ACC026", "Test Account 26", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account27 = Account.create("ACC027", "Test Account 27", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account28 = Account.create("ACC028", "Test Account 28", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account29 = Account.create("ACC029", "Test Account 29", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account30 = Account.create("ACC030", "Test Account 30", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account31 = Account.create("ACC031", "Test Account 31", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account32 = Account.create("ACC032", "Test Account 32", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account33 = Account.create("ACC033", "Test Account 33", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account34 = Account.create("ACC034", "Test Account 34", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account35 = Account.create("ACC035", "Test Account 35", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account36 = Account.create("ACC036", "Test Account 36", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account37 = Account.create("ACC037", "Test Account 37", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account38 = Account.create("ACC038", "Test Account 38", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account39 = Account.create("ACC039", "Test Account 39", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account40 = Account.create("ACC040", "Test Account 40", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account41 = Account.create("ACC041", "Test Account 41", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account42 = Account.create("ACC042", "Test Account 42", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account43 = Account.create("ACC043", "Test Account 43", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account44 = Account.create("ACC044", "Test Account 44", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account45 = Account.create("ACC045", "Test Account 45", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account46 = Account.create("ACC046", "Test Account 46", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account47 = Account.create("ACC047", "Test Account 47", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account48 = Account.create("ACC048", "Test Account 48", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account49 = Account.create("ACC049", "Test Account 49", new BigDecimal("10000000.00"), AccountType.SAVINGS);
        Account account50 = Account.create("ACC050", "Test Account 50", new BigDecimal("10000000.00"), AccountType.SAVINGS);

        // 保存账户到内存中
        accounts.put(account1.accountNumber(), account1);
        accounts.put(account2.accountNumber(), account2);
        accounts.put(account3.accountNumber(), account3);
        accounts.put(account4.accountNumber(), account4);
        accounts.put(account5.accountNumber(), account5);
        accounts.put(account6.accountNumber(), account6);
        accounts.put(account7.accountNumber(), account7);
        accounts.put(account8.accountNumber(), account8);
        accounts.put(account9.accountNumber(), account9);
        accounts.put(account10.accountNumber(), account10);
        accounts.put(account11.accountNumber(), account11);
        accounts.put(account12.accountNumber(), account12);
        accounts.put(account13.accountNumber(), account13);
        accounts.put(account14.accountNumber(), account14);
        accounts.put(account15.accountNumber(), account15);
        accounts.put(account16.accountNumber(), account16);
        accounts.put(account17.accountNumber(), account17);
        accounts.put(account18.accountNumber(), account18);
        accounts.put(account19.accountNumber(), account19);
        accounts.put(account20.accountNumber(), account20);
        accounts.put(account21.accountNumber(), account21);
        accounts.put(account22.accountNumber(), account22);
        accounts.put(account23.accountNumber(), account23);
        accounts.put(account24.accountNumber(), account24);
        accounts.put(account25.accountNumber(), account25);
        accounts.put(account26.accountNumber(), account26);
        accounts.put(account27.accountNumber(), account27);
        accounts.put(account28.accountNumber(), account28);
        accounts.put(account29.accountNumber(), account29);
        accounts.put(account30.accountNumber(), account30);
        accounts.put(account31.accountNumber(), account31);
        accounts.put(account32.accountNumber(), account32);
        accounts.put(account33.accountNumber(), account33);
        accounts.put(account34.accountNumber(), account34);
        accounts.put(account35.accountNumber(), account35);
        accounts.put(account36.accountNumber(), account36);
        accounts.put(account37.accountNumber(), account37);
        accounts.put(account38.accountNumber(), account38);
        accounts.put(account39.accountNumber(), account39);
        accounts.put(account40.accountNumber(), account40);
        accounts.put(account41.accountNumber(), account41);
        accounts.put(account42.accountNumber(), account42);
        accounts.put(account43.accountNumber(), account43);
        accounts.put(account44.accountNumber(), account44);
        accounts.put(account45.accountNumber(), account45);
        accounts.put(account46.accountNumber(), account46);
        accounts.put(account47.accountNumber(), account47);
        accounts.put(account48.accountNumber(), account48);
        accounts.put(account49.accountNumber(), account49);
        accounts.put(account50.accountNumber(), account50);
    }

    public Transaction saveTransaction(Transaction transaction) {
        Long id = transactionIdGenerator.getAndIncrement();
        Transaction newTransaction = new Transaction(
            id,
            transaction.fromAccount(),
            transaction.toAccount(),
            transaction.amount(),
            transaction.type(),
            transaction.timestamp(),
            transaction.status()
        );
        transactions.put(id, newTransaction);
        return newTransaction;
    }

    public Transaction findTransactionById(Long id) {
        return transactions.get(id);
    }

    public List<Transaction> findAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public void deleteTransaction(Long id) {
        transactions.remove(id);
    }

    public Account saveAccount(Account account) {
        accounts.put(account.accountNumber(), account);
        return account;
    }

    public Account findAccountByNumber(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean validateAccountBalance(String accountNumber, BigDecimal amount) {
        accountLock.readLock().lock();
        try {
            Account account = accounts.get(accountNumber);
            return account != null && account.balance().compareTo(amount) >= 0;
        } finally {
            accountLock.readLock().unlock();
        }
    }

    public void updateAccountBalances(String fromAccount, String toAccount, BigDecimal amount) {
        accountLock.writeLock().lock();
        try {
            Account from = accounts.get(fromAccount);
            Account to = accounts.get(toAccount);
            
            if (from == null || to == null) {
                throw new IllegalArgumentException("Account not found");
            }
            
            if (from.balance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            
            // 原子性更新账户余额
            Account updatedFrom = from.withBalance(from.balance().subtract(amount));
            Account updatedTo = to.withBalance(to.balance().add(amount));
            
            // 使用原子操作更新账户
            accounts.put(fromAccount, updatedFrom);
            accounts.put(toAccount, updatedTo);
        } finally {
            accountLock.writeLock().unlock();
        }
    }

    public List<Account> findAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
}
