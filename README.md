# Hospital Primary Care Database System

## Overview
This project is a database-driven application designed to manage the primary care operations of a hospital serving a retirement community. It models patients, admissions, employees, diagnoses, treatments, and room usage.

The system uses a **MySQL relational database** and a **Java Swing GUI** to allow users to execute SQL queries and view results in an easy-to-use interface.

---

## Features
- Relational database design based on ER modeling
- Supports multiple patient admissions over time
- Tracks emergency contact and insurance per admission
- Assigns primary and additional doctors to patients
- Records treatment orders and administrations
- Supports multiple employees administering treatments
- GUI-based query execution (no command line required)
- Input validation and exception handling

---

## Technologies Used
- Java (JDK 23)
- MySQL
- JDBC (MySQL Connector/J)
- Swing (GUI)
- Maven

---

## Database Setup

1. Open MySQL Workbench or terminal
2. Run:

```sql
CREATE DATABASE hospital_db;
Ensure your MySQL server is running
Application Setup
Clone or download this repository
Open the project in IntelliJ IDEA
Update database credentials in HospitalPrimaryCareApp.java:
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
Reload Maven dependencies

Run:
HospitalPrimaryCareApp.java
How It Works
On startup, the application connects to MySQL
It creates all required tables if they do not exist
It seeds the database with sample data (only if empty)
Users select a query from a dropdown menu
Optional input fields allow filtering (e.g., patient ID)
Results are displayed in the application window
Example Queries Demonstrated
Room utilization (occupied/unoccupied)
Patient admissions and treatment history
Readmissions within 30 days
Treatment frequency and aggregation
Employee activity and doctor-specific reports
Exception Handling
Prevents queries from running with missing required inputs
Displays user-friendly error messages instead of crashing
Demo Instructions
Run the application
Select a report from the dropdown
Enter required input values (if applicable)
Click "Run Selected Query"
View results in the output panel
Try leaving a required field blank to see validation

Notes
The database runs locally (localhost)
No external server or deployment is required
SQLite was used during development, but final version uses MySQL
