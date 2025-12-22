# ClinAlert - Plateforme de Suivi Médical et Gestion des Alertes

## 📋 Description du Projet

**ClinAlert** est une plateforme web complète de gestion médicale permettant le suivi des patients, la gestion des cliniques, et un système d'alertes en temps réel basé sur des données de santé provenant de montres connectées.

### 🎯 Fonctionnalités Principales

- **Authentification Multi-Rôles** : Doctor, Patient, Admin
- **Gestion CRUD** : Patients, Cliniques, Utilisateurs
- **Système d'Alertes** : Détection automatique d'anomalies (CRITICAL, HIGH, MEDIUM)
- **Intégration SmartWatch** : Réception données de santé (FC, SpO2, température)
- **Dashboard Personnalisé** : Vue spécifique par rôle (Doctor/Patient)
- **Rapports PDF** : Génération automatique de rapports médicaux

---

## 🏗️ Architecture Technique

### **Backend** : Spring Boot 3.2.0 (Java 17 LTS)
- **Framework** : Spring Boot, Spring Security, Spring Data JPA
- **Base de données** : H2 (dev), PostgreSQL (prod)
- **API REST** : Endpoints CRUD + Authentification JWT
- **Tests** : JUnit 5, Mockito (291 tests unitaires, 84.3% coverage)
- **Quality Gate** : SonarCloud PASSED ✅

### **Frontend** : Flutter 3.9.2 (Web)
- **Framework** : Flutter Web
- **State Management** : Provider/setState
- **Routing** : Flutter Router
- **API Client** : HTTP package
- **Tests** : 15 tests Selenium automatisés (100% PASSED)

### **Tests Selenium** : Automatisation Frontend
- **Framework** : Selenium WebDriver 4.17.0 + TestNG 7.9.0
- **Pattern** : Page Object Model (POM)
- **Sélecteurs** : XPath + CSS
- **Reporting** : Allure 2.25.0 avec screenshots
- **Scénarios** : 20 tests (Auth, CRUD, Navigation, Security, E2E)

---

## 📁 Structure du Projet

```
Projet_Clinalert/
├── backend/
│   ├── doctortracker-backend/          # API Spring Boot
│   │   ├── src/main/java/
│   │   │   └── com/clinalert/doctortracker/
│   │   │       ├── controller/         # REST Controllers
│   │   │       ├── service/            # Business Logic
│   │   │       ├── repository/         # JPA Repositories
│   │   │       ├── model/              # Entities
│   │   │       ├── config/             # Security, CORS
│   │   │       └── util/               # HMAC, PDF Generator
│   │   ├── src/test/java/              # 291 tests unitaires
│   │   └── pom.xml                     # Maven dependencies
│   │
│   └── clinalert-selenium-tests/       # Tests Selenium
│       ├── src/main/java/
│       │   └── com/clinalert/
│       │       ├── config/             # WebDriverConfig, TestConfig
│       │       ├── pages/              # 7 Page Objects (XPath)
│       │       └── utils/              # WaitHelper, ScreenshotUtil
│       ├── src/test/java/
│       │   └── com/clinalert/tests/    # 6 test classes (20 scenarios)
│       ├── pom.xml                     # Selenium + TestNG + Allure
│       └── testng.xml                  # Test suite configuration
│
├── frontend/
│   └── doctortracker_frontend/         # Application Flutter Web
│       ├── lib/
│       │   ├── screens/                # Login, Dashboards, CRUD
│       │   ├── models/                 # Data models
│       │   ├── services/               # API calls
│       │   └── widgets/                # Reusable components
│       ├── pubspec.yaml                # Flutter dependencies
│       └── web/                        # HTML, assets
│
└── docs/
    └── rapport/                        # Documentation PAQL (LaTeX)
        ├── main.tex                    # Rapport principal
        ├── chapter_selenium.tex        # Chapitre tests Selenium
        └── images/                     # Screenshots tests
```

---

## 🚀 Installation et Démarrage

### Prérequis

- **Java JDK 17+**
- **Maven 3.8+**
- **Flutter SDK 3.9.2+**
- **Chrome Browser** (pour Selenium)
- **Git**

### 1️⃣ **Backend (Spring Boot)**

```bash
cd backend/doctortracker-backend

# Installer dépendances
mvn clean install

# Lancer l'application
mvn spring-boot:run

# URL: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
```

### 2️⃣ **Frontend (Flutter Web)**

```bash
cd frontend/doctortracker_frontend

# Installer dépendances
flutter pub get

# Lancer en mode dev
flutter run -d chrome --web-port=57056

# URL: http://localhost:57056
```

### 3️⃣ **Tests Selenium**

```bash
cd backend/clinalert-selenium-tests

# Exécuter tests
mvn clean test

# Générer rapport Allure
mvn allure:serve
```

---

## 🧪 Tests & Qualité

### **Backend** - 291 Tests Unitaires

```bash
mvn test
```

- **Coverage** : 84.3% (SonarCloud)
- **Frameworks** : JUnit 5, Mockito
- **Tests** : Controllers, Services, Repositories, Utils

### **Frontend** - 20 Scénarios Selenium

```bash
mvn test -f backend/clinalert-selenium-tests/pom.xml
```

#### Scénarios Couverts

| Catégorie | Scénarios | Status |
|-----------|-----------|--------|
| **Authentication** | 6 (3 positifs + 3 négatifs) | ✅ Implémentés |
| **CRUD Patients** | 4 (CREATE, READ, UPDATE*, DELETE*) | ⚠️ 2/4 (*à implémenter UI) |
| **CRUD Cliniques** | 4 (CREATE, READ, UPDATE*, DELETE*) | ⚠️ 2/4 (*à implémenter UI) |
| **Navigation** | 2 (Dashboard → Patients/Alerts) | ✅ Implémentés |
| **Sécurité** | 4 (Isolation, RBAC, URL, SQL injection) | ✅ Implémentés |
| **E2E** | 1 (Workflow complet) | ✅ Implémenté |
| **TOTAL** | **20 scénarios** | **17 actifs** |

---

## 👥 Utilisateurs de Test

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| `admin@clinalert.com` | `admin123` | ADMIN |
| `house@clinalert.com` | `doctor123` | DOCTOR |
| `cameron@clinalert.com` | `doctor123` | DOCTOR |
| `john.doe@clinalert.com` | `patient123` | PATIENT |
| `luc.moreau@clinalert.com` | `patient123` | PATIENT |

---

## 📊 Qualité du Code

### SonarCloud Analysis

- **Quality Gate** : ✅ PASSED
- **Coverage** : 84.3%
- **Tests** : 291 unitaires
- **Bugs** : 0
- **Vulnerabilities** : 0
- **Code Smells** : 44 (Reliability)
- **Duplications** : 3.0%

### Selenium Tests Results

- **Tests Exécutés** : 15/20
- **Success Rate** : 100% ✅
- **Functional Coverage** : 85%
- **Duration** : 13min 35s
- **Screenshots** : 13 captured

---

## 📚 Documentation

- **Rapport PAQL** : `docs/rapport/main.tex` (LaTeX)
- **API Endpoints** : Swagger UI (dev uniquement)
- **Tests Selenium** : `docs/rapport/chapter_selenium.tex`
- **Architecture** : Diagrammes dans rapport PAQL

---

## 🔒 Sécurité

- **JWT Authentication** : Tokens sécurisés
- **Spring Security** : RBAC (Role-Based Access Control)
- **HMAC Signature** : Validation SmartWatch data
- **CORS Configuration** : Allowlisted origins
- **Password Hashing** : BCrypt
- **SQL Injection Protection** : JPA parameterized queries

---

## 🛠️ Technologies Utilisées

### Backend
- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- H2 Database
- iText PDF 5.5.13
- Lombok
- JUnit 5 + Mockito

### Frontend
- Flutter 3.9.2
- Dart 3.x
- HTTP package
- Provider (state management)

### Tests
- Selenium WebDriver 4.17.0
- TestNG 7.9.0
- WebDriverManager 5.6.3
- Allure 2.25.0
- SonarCloud

---

## 📝 Licence

Ce projet est développé dans un cadre académique.

---

## 👨‍💻 Auteur

**ClinAlert Team**  
Projet de gestion médicale avec monitoring en temps réel

---

## 📞 Support

Pour toute question ou problème :
- Ouvrir une issue sur GitHub
- Consulter la documentation dans `/docs`

---

**Version** : 2.0 - Tests Backend + Frontend Validés  
**Date** : Décembre 2024
