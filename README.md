# 🎓 University ERP System (Java + Swing)

A desktop-based **University ERP (Enterprise Resource Planning)** system developed using **Java and Swing**, designed to manage courses, sections, enrollments, and grades with **role-based access control** and **secure authentication**.

The project follows a **layered architecture**, separates **authentication data from academic data**, and supports a global **Maintenance Mode** to ensure data safety during administrative operations.

---

## 📌 Features Overview

### 👤 User Roles

#### Student
- Browse course catalog
- Register and drop sections (capacity & deadline enforced)
- View timetable
- View grades (quiz, mid-sem, end-sem, final)
- Download transcript (CSV)

#### Instructor
- View assigned sections only
- Enter assessment scores
- Compute final grades using defined weight rules
- View class statistics

#### Admin
- Add and manage users (students & instructors)
- Create and manage courses and sections
- Assign instructors to sections
- Toggle Maintenance Mode
- Backup and restore ERP database

---

## 🏗️ Project Architecture

The system follows a clean **layered architecture**:

UI Layer (Swing Frames)  
↓  
Access / Service Layer  
↓  
DAO Layer  
↓  
Databases (Auth DB + ERP DB)

---

## 🔐 Authentication & Security Design

- The system uses **two separate databases**:
  - **Auth Database**
    - Stores username, role, password hash
    - No academic data
  - **ERP Database**
    - Stores students, instructors, courses, sections, enrollments, grades
    - Never stores passwords

- Password handling:
  - Passwords are never stored in plaintext
  - Only secure hashes are stored
  - Password verification uses one-way hashing
  - Follows a UNIX “shadow password” style design

- Authentication and authorization are fully decoupled

---

## 🛠️ Technologies Used

- Java (JDK 17 or later)
- Java Swing (Desktop UI)
- Maven (Build & dependency management)
- JDBC (Database connectivity)
- MySQL / MariaDB (or compatible RDBMS)

---

## ▶️ How to Run the Project

### 1️⃣ Prerequisites

- Java JDK installed
- Maven installed (`mvn -v` should work)
- Database configured and running

---

### 2️⃣ Clone the Repository

```bash
git clone https://github.com/<your-username>/UniversityERP.git
cd UniversityERP
```



## 🧪 Validation & Safety Checks

- Duplicate enrollments are prevented  
- Section capacity is strictly enforced  
- Deadline-based drop restrictions are applied  
- Role-based access control is enforced at every write operation  
- Maintenance Mode blocks all student and instructor modifications  
- Clear and user-friendly error messages are shown  

---

## 🔧 Maintenance Mode

- Controlled by the **Admin**
- When enabled:
  - Students and Instructors can only view data
  - All create / update / delete operations are blocked
- A visible banner is shown across the UI
- Ensures consistency during maintenance or backup operations

---

## 💾 Backup & Restore

- ERP database can be backed up by the Admin
- Restore functionality reverts the database to a previous state
- Useful for demos, testing, and recovery

---

## 📄 Documentation Included

- UML and architecture diagrams
- Test plan and test summary
- Seed scripts
- Demo video
- How-to-run guide

---

## 🎯 Learning Outcomes

- DAO pattern and layered architecture
- Secure authentication and authorization
- Java Swing UI development
- Maven-based project structure
- Real-world ERP system design principles


