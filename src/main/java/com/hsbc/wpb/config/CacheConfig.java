package com.hsbc.wpb.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.hsbc.wpb.repository.InMemoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineConfig());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                // 设置写入后过期时间
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 设置访问后过期时间
                .expireAfterAccess(10, TimeUnit.MINUTES)
                // 初始容量
                .initialCapacity(1000)
                // 最大容量
                .maximumSize(100000)
                // 开启统计
                .recordStats()
                // 开启弱引用，允许垃圾回收
                .weakKeys()
                .weakValues();
    }

    // 缓存预热任务
    @Bean
    public CacheWarmUpService cacheWarmUpService(CacheManager cacheManager, InMemoryRepository repository) {
        return new CacheWarmUpService(cacheManager, repository);
    }

    // 缓存监控任务
    @Bean
    public CacheMonitorService cacheMonitorService(CacheManager cacheManager) {
        return new CacheMonitorService(cacheManager);
    }
}

// 缓存预热服务
class CacheWarmUpService {
    private final CacheManager cacheManager;
    private final InMemoryRepository repository;
    private static final Logger log = LoggerFactory.getLogger(CacheWarmUpService.class);

    public CacheWarmUpService(CacheManager cacheManager, InMemoryRepository repository) {
        this.cacheManager = cacheManager;
        this.repository = repository;
    }

    // 应用启动时执行缓存预热
    @PostConstruct
    public void warmUp() {
        log.info("Starting cache warm-up...");
        try {
            // 预热交易列表缓存
            repository.findAllTransactions().forEach(transaction -> {
                cacheManager.getCache("transactions").put(transaction.id(), transaction);
            });
            log.info("Transaction cache warm-up completed");

            // 预热账户缓存
            repository.findAllAccounts().forEach(account -> {
                cacheManager.getCache("accounts").put(account.accountNumber(), account);
            });
            log.info("Account cache warm-up completed");

            // 预热交易列表缓存
            cacheManager.getCache("transactionList").put("all", repository.findAllTransactions());
            log.info("Transaction list cache warm-up completed");
        } catch (Exception e) {
            log.error("Cache warm-up failed", e);
        }
    }

    // 定时预热（每天凌晨2点执行）
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledWarmUp() {
        log.info("Starting scheduled cache warm-up...");
        warmUp();
    }
}

// 缓存监控服务
class CacheMonitorService {
    private final CacheManager cacheManager;
    private static final Logger log = LoggerFactory.getLogger(CacheMonitorService.class);

    public CacheMonitorService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // 每5分钟监控一次缓存状态
    @Scheduled(fixedRate = 300000)
    public void monitorCache() {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        Map<String, CacheStats> stats = new HashMap<>();

        // 获取所有缓存的统计信息
        caffeineCacheManager.getCacheNames().forEach(name -> {
            com.github.benmanes.caffeine.cache.Cache<?, ?> cache = 
                (com.github.benmanes.caffeine.cache.Cache<?, ?>) caffeineCacheManager.getCache(name).getNativeCache();
            stats.put(name, cache.stats());
        });

        // 记录缓存统计信息
        stats.forEach((name, stat) -> {
            log.info("Cache '{}' stats:", name);
            log.info("  Hit rate: {:.2f}%", stat.hitRate() * 100);
            log.info("  Miss rate: {:.2f}%", stat.missRate() * 100);
            log.info("  Average load penalty: {} ms", stat.averageLoadPenalty() / 1_000_000);
            log.info("  Eviction count: {}", stat.evictionCount());
        });

        // 监控内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        log.info("Memory usage:");
        log.info("  Total memory: {} MB", totalMemory / (1024 * 1024));
        log.info("  Used memory: {} MB", usedMemory / (1024 * 1024));
        log.info("  Free memory: {} MB", freeMemory / (1024 * 1024));
        log.info("  Max memory: {} MB", runtime.maxMemory() / (1024 * 1024));
    }
} 