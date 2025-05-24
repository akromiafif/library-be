package com.samsung.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Main Spring Boot Application class for Digital Library Management System
 *
 * Features enabled:
 * - JPA Auditing for automatic timestamps
 * - Caching for improved performance
 * - Async processing for non-blocking operations
 * - Scheduled tasks for maintenance operations
 * - Transaction management
 * - Configuration properties binding
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties
public class LibraryApplication {

	/**
	 * Main method to start the Digital Library application
	 */
	public static void main(String[] args) {
		// Set system properties before starting the application
		System.setProperty("spring.application.name", "Digital Library Management System");
		System.setProperty("spring.devtools.restart.enabled", "false");
		System.setProperty("spring.profiles.active",
				System.getProperty("spring.profiles.active", "dev"));

		try {
			SpringApplication app = new SpringApplication(LibraryApplication.class);

			// Add startup banner
			app.setBanner((environment, sourceClass, out) -> {
				out.println("================================================");
				out.println("   📚 DIGITAL LIBRARY MANAGEMENT SYSTEM 📚   ");
				out.println("================================================");
				out.println("   Version: 1.0.0                            ");
				out.println("   Profile: " + environment.getActiveProfiles()[0]);
				out.println("   Java Version: " + System.getProperty("java.version"));
				out.println("================================================");
			});

			// Run the application
			var context = app.run(args);

			// Log successful startup
			String port = context.getEnvironment().getProperty("server.port", "8080");
			String contextPath = context.getEnvironment().getProperty("server.servlet.context-path", "");

			System.out.println("\n🚀 Digital Library System started successfully!");
			System.out.println("📍 Application URL: http://localhost:" + port + contextPath);
			System.out.println("📊 API Documentation: http://localhost:" + port + contextPath + "api");
			System.out.println("💾 Database: " + context.getEnvironment().getProperty("spring.datasource.url"));
			System.out.println("🔧 Active Profile: " + String.join(",", context.getEnvironment().getActiveProfiles()));

		} catch (Exception e) {
			System.err.println("❌ Failed to start Digital Library System: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Post-construct initialization
	 * Sets up global application configuration
	 */
	@PostConstruct
	public void initialize() {
		// Set default timezone to UTC for consistent date/time handling
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		// Log initialization
		System.out.println("🔧 Digital Library System initialized with UTC timezone");
	}
}