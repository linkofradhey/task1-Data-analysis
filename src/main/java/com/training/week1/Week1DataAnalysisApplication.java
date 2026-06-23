package com.training.week1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Week1DataAnalysisApplication
 *
 * Entry point for the Spring Boot app. Run this class directly from STS
 * (Right-click -> Run As -> Spring Boot App) and it starts ONE embedded
 * Tomcat server on port 8080 that serves both:
 *   - the REST API (/api/summary, /api/missing, /api/dtypes, /api/raw)
 *   - the frontend (index.html, served automatically from
 *     src/main/resources/static/ by Spring Boot's default static
 *     resource handling)
 *
 * No separate frontend server needed - everything runs under this
 * single STS-managed process.
 */
@SpringBootApplication
public class Week1DataAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(Week1DataAnalysisApplication.class, args);
    }
}
