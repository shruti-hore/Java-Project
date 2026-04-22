# 🧠 PROJECT CODE EXTRACTION & ANALYSIS PROMPT

You are an expert software engineer, code reviewer, and system architect.

Your task is to analyze a given project folder and extract a COMPLETE understanding of the system, focusing on:
1. UI / Frontend (JavaFX)
2. Application Architecture
3. Cryptography Module

This is NOT a simple summary task. Perform a structured deep analysis.

---

# 🎯 OBJECTIVE

Extract:
- Code structure
- Design components
- Data flow
- Dependencies
- Missing/weak areas

The output should help a developer:
- Understand the entire system quickly
- Identify what is implemented vs missing
- Plan future development

---

# 📁 INPUT

You will be given:
- A project folder (multiple files and directories)

Assume:
- Java-based project
- JavaFX UI
- Service layer
- Database integration (MongoDB or similar)
- Cryptography module (for encryption/security)

---

# 🧩 PART 1: PROJECT STRUCTURE

Extract and present:

## Folder Structure
- List all folders and files
- Group by:
  - UI
  - Model
  - Service
  - Database
  - Utility
  - Cryptography

## Purpose of Each Folder
Explain:
- What each folder is responsible for
- How they interact

---

# 🖥️ PART 2: UI / FRONTEND ANALYSIS

## Components Identification
List all UI components:
- Screens (Dashboard, Forms, etc.)
- Panels (Sidebar, Topbar, Columns)
- Reusable components (TaskCard, Buttons, etc.)

## Layout System
Explain:
- Which JavaFX layouts are used (VBox, HBox, GridPane, etc.)
- How the UI is structured visually

## UI Flow
Describe:
- How user navigates through the app
- What happens when:
  - Add task
  - Edit task
  - Delete task
  - Change status

## Event Handling
- How button clicks are handled
- Where logic is triggered

## UI Issues (Important)
Identify:
- Tight coupling with backend
- Code duplication
- Poor separation of concerns
- Styling issues

---

# 🧱 PART 3: ARCHITECTURE ANALYSIS

## Layered Design
Explain flow:
UI → Service → Database

Check:
- Is UI directly calling DB? (bad practice)
- Is Service layer properly used?

## Class Responsibilities
For key classes:
- DashboardUI
- TaskService
- MongoService (or DB layer)

Explain:
- What each class does
- Whether responsibilities are correct

## Data Flow
Explain:
- How data moves from UI to DB and back

---

# 🔐 PART 4: CRYPTOGRAPHY MODULE ANALYSIS

## Identification
- Locate all cryptography-related files/classes

## Functionality
Explain:
- What encryption is used (AES, RSA, etc.)
- What is being encrypted:
  - Tasks?
  - User data?
  - Communication?

## Implementation Details
- How encryption/decryption is done
- Where keys are stored
- How keys are generated

## Security Evaluation
Check for:
- Hardcoded keys ❌
- Weak algorithms ❌
- Missing error handling ❌

## Integration
- How crypto module connects with:
  - UI
  - Service layer
  - Database

## Issues / Gaps
Clearly list:
- Missing features
- Security risks
- Incomplete implementation

---

# 📊 PART 5: FEATURE SUMMARY

## Implemented Features
List clearly:
- Task CRUD
- Status updates
- UI dashboard
- Any encryption features

## Partially Implemented
- Features started but not complete

## Missing Features
- Important features not present

---

# 🧹 PART 6: CODE QUALITY REVIEW

Evaluate:
- Naming conventions
- Code duplication
- Modularity
- Readability

---

# ⚠️ PART 7: PROBLEMS & RISKS

List:
- Architectural issues
- Performance concerns
- Security issues (especially crypto)

---

# 🚀 PART 8: IMPROVEMENT OPPORTUNITIES

Suggest:
- UI improvements
- Architecture fixes
- Crypto enhancements
- Feature additions

---

# 📌 OUTPUT FORMAT

Use structured sections:

1. Project Overview
2. Folder Structure
3. UI Analysis
4. Architecture Analysis
5. Cryptography Module Analysis
6. Feature Summary
7. Code Quality Review
8. Problems & Risks
9. Improvement Suggestions

Be:
- Clear
- Structured
- Technical
- No vague explanations

---

# 🎯 FINAL GOAL

Produce a document that acts as a:
- Technical blueprint
- Reverse-engineered documentation
- Base for future development planning