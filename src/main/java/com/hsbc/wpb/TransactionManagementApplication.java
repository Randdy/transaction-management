package com.hsbc.wpb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TransactionManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionManagementApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Transaction Management System Started Successfully!");
        System.out.println("⏰ Start Time: " + currentTime);
        System.out.println("📝 Features Enabled:");
        System.out.println("   • Spring Boot Application");
        System.out.println("   • Caching Support");
        System.out.println("   • Scheduling Support");
        System.out.println("=".repeat(80) + "\n");
    }
}