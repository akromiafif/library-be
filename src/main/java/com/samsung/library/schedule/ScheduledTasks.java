package com.samsung.library.schedule;

import com.samsung.library.service.BorrowedBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled tasks for Digital Library maintenance operations
 * Can be enabled/disabled via configuration properties
 */
@Service
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BorrowedBookService borrowedBookService;

    /**
     * Update overdue books status every day at midnight
     * Cron: 0 0 0 * * * (second minute hour day month weekday)
     */
    @Scheduled(cron = "${app.scheduling.overdue-books-cron:0 0 0 * * *}")
    public void updateOverdueBooks() {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("🔄 [{}] Starting scheduled task: Update overdue books", timestamp);

        try {
            borrowedBookService.updateOverdueBooks();
            logger.info("✅ [{}] Overdue books status updated successfully", timestamp);
        } catch (Exception e) {
            logger.error("❌ [{}] Failed to update overdue books status", timestamp, e);
        }
    }

    /**
     * Log system health every hour
     * Cron: 0 0 * * * * (every hour at minute 0)
     */
    @Scheduled(cron = "${app.scheduling.health-check-cron:0 0 * * * *}")
    public void logSystemHealth() {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("💚 [{}] Digital Library System is running", timestamp);

        // Log memory usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;

        logger.info("📊 [{}] Memory usage: {} MB used, {} MB free, {} MB total",
                timestamp, usedMemory, freeMemory, totalMemory);
    }

    /**
     * Cleanup completed tasks every Sunday at 2 AM
     * This is a placeholder for future cleanup operations
     */
    @Scheduled(cron = "${app.scheduling.cleanup-cron:0 0 2 * * SUN}")
    public void performWeeklyCleanup() {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("🧹 [{}] Starting weekly cleanup tasks", timestamp);

        try {
            // Placeholder for cleanup operations
            // Could include: log file rotation, temporary file cleanup, etc.
            logger.info("✅ [{}] Weekly cleanup completed successfully", timestamp);
        } catch (Exception e) {
            logger.error("❌ [{}] Weekly cleanup failed", timestamp, e);
        }
    }
}