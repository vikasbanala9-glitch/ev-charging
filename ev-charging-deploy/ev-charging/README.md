# EV Charging Station Management System
Stack: Java 17, Spring Boot 3, Spring Data JPA, H2 (MySQL-ready), HTML/CSS/JS frontend.
Run: `mvn spring-boot:run` then open http://localhost:8080  (admin: admin@ev.com (password: ADMIN_PASSWORD env var, default admin123))
Structure: entities (User, Station, Charger, Booking) -> repositories (Repos.java) -> REST API (Api.java) -> static UI.
Notes: payment and energy metering are simulated; passwords use SHA-256 and the header X-User-Id acts as a simple session (use Spring Security + JWT + BCrypt for production).
