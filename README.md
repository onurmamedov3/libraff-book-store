📚 Libraff Book Store Management API

A robust, enterprise-level RESTful API built with Spring Boot for managing a multi-branch bookstore network. This system handles everything from inventory and inter-branch transfers to HR operations, automated payroll, and dynamic discount processing.
✨ Core Features & Modules
1. 👥 Human Resources (HR) & Employee Management

    Lifecycle Management: Add, update, rehire, and softly delete (deactivate) employees.

    Position Constraints: Enforces strict staff limits per store (e.g., max 1 Store Manager, 2 Cashiers, 3 Sales Reps) automatically during hiring and transfers.

    Work History Tracking: Automatically closes previous work chapters and opens new timelines when an employee changes roles, salaries, or branches.

2. 📦 Inventory & Inter-Branch Transfers

    Catalog Management: Manages books, authors, and genres.

    Branch Stock: Tracks specific book quantities per store.

    Transfer Approval Workflow: Employees can request book transfers from other branches. Store Managers must approve or reject these requests. Approved transfers automatically deduct stock from the source and add it to the destination.

3. 💰 Sales & Dynamic Discounts

    Transactions: Processes book sales, verifies stock availability, and automatically deducts sold items from the specific branch's inventory.

    Smart Discounts: Supports complex, time-bound discounts (5% to 40%) targeting specific Books, Authors, Genres, or Stores. The system calculates the best active discount dynamically at checkout.

4. 🏦 Automated Payroll & Bonus System (Grading)

    Automated Cron Jobs: Runs a scheduled payroll task on the 1st of every month to process salaries.

    Pro-rated Salaries: Automatically calculates partial salaries for employees hired mid-month based on days worked.

    Performance Bonuses: Calculates sales-based bonuses based on configurable "Grade Structures" (thresholds and percentages) applied at either the Employee or Store level. Tracks salary and bonus history securely.

🛠️ Tech Stack & Architecture

    Framework: Java, Spring Boot 3+ 

    Data Access: Spring Data JPA, Hibernate (relational mapping, lazy loading) 

    Validation: Jakarta Validation (@Valid, @NotBlank, @PastOrPresent) 

    DTO Mapping: ModelMapper (configured with STRICT matching strategy) 

    Boilerplate Reduction: Lombok (@Data, @Getter, @Setter, @RequiredArgsConstructor) 

    Task Scheduling: Spring @EnableScheduling / @Scheduled 

📡 API Endpoints Overview

The API is structured around well-defined domain controllers:
Employees (/employees)

    POST /employees - Register a new employee.

    GET /employees - List all employees.

    PATCH /employees - Update details (triggers history updates and limit checks).

    DELETE /employees/{id} - Soft delete/fire an employee.

    PATCH /employees/{id} - Rehire an inactive employee.

Books & Inventory (/books, /transfers)

    POST /books - Add a new book to the catalog.

    POST /transfers/request - Request a book transfer between stores.

    PATCH /transfers/{transferId}/approve - Manager approval/rejection of a transfer.

Sales & Promotions (/transaction-history, /discounts)

    POST /transaction-history/sell - Register a sale and update stock.

    POST /discounts - Create a new time-bound promotional discount.

Payroll (/payroll)

    Automated internally via Cron job, but includes a controller for manual overrides/testing. 

🛡️ Exception Handling & Validation

The application features a centralized, robust error-handling mechanism:

    @RestControllerAdvice: Custom MyHandler intercepts all exceptions.

    Structured Error Responses: Returns standard JSON containing a unique GUID, error code, timestamp, requested path, and a list of specific field validation failures.

    Custom Exceptions: MyException wrapper allows throwing precise HTTP status codes and business logic errors (e.g., POSITION_LIMIT_EXCEEDED, INSUFFICIENT_STOCK).
