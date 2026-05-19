# 🛡️ Enterprise KYC Verification Portal

A full-stack, enterprise-grade Know Your Customer (KYC) verification system built with **Java** and **Spring Boot**. This application simulates a real-world document verification workflow, featuring role-based access control, secure file uploads, email OTP identity verification, and a custom Bootstrap frontend.

## ✨ Key Features

* **Role-Based Access Control (RBAC):** Strict separation of duties between Citizens (submitters) and Admins (approvers) using Spring Security.
* **Email OTP Verification:** Simulates an identity verification layer where users must request and validate a 6-digit one-time password before submitting sensitive data.
* **Secure Document Uploads:** Handles `MultipartFile` uploads securely, saving files to the server and linking them to database records via unique UUIDs to prevent overwriting.
* **Dynamic In-Memory Authentication:** Custom implementation of `UserDetailsService` allowing dynamic user registration at runtime with BCrypt password hashing.
* **Global Exception Handling:** Custom `@ControllerAdvice` intercepts validation errors and bad requests, returning clean, readable JSON responses to the frontend.
* **Interactive Dashboard:** A responsive HTML/JS frontend utilizing Bootstrap to dynamically render different UIs based on the logged-in user's role.

## 🛠️ Technology Stack

* **Backend:** Java 17+, Spring Boot 3.x
* **Security:** Spring Security, BCrypt Password Encoding
* **Database:** H2 In-Memory Database (Easily swappable to MySQL/PostgreSQL via `application.properties`)
* **Data Access:** Spring Data JPA / Hibernate
* **Frontend:** HTML5, CSS3, Vanilla JavaScript, Bootstrap 5
* **API:** RESTful Architecture

## 🚀 How to Run the Application

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/shrutika1195/enterprise-kyc-system.git](https://github.com/shrutika1195/enterprise-kyc-system.git)
   cd enterprise-kyc-system

   mvn spring-boot:run

   
