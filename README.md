<p align="center">
  <img src="docs/images/architecture_diagram.png" alt="ClinAlert Logo" width="600"/>
</p>

<h1 align="center">🏥 ClinAlert</h1>

<p align="center">
  <strong>Système Intelligent de Suivi Médical & Monitoring de Santé</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Flutter-3.x-02569B?style=for-the-badge&logo=flutter&logoColor=white" alt="Flutter"/>
  <img src="https://img.shields.io/badge/Dart-3.x-0175C2?style=for-the-badge&logo=dart&logoColor=white" alt="Dart"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/JWT-Security-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Web-brightgreen?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"/>
  <img src="https://img.shields.io/badge/Status-Production%20Ready-success?style=flat-square" alt="Status"/>
</p>

---

## 📋 Table des Matières

- [🎯 À Propos](#-à-propos)
- [✨ Fonctionnalités](#-fonctionnalités)
- [🏗️ Architecture](#️-architecture)
- [📱 Captures d'Écran](#-captures-décran)
- [🚀 Installation](#-installation)
- [⚙️ Configuration](#️-configuration)
- [🔐 Sécurité](#-sécurité)
- [📚 Documentation API](#-documentation-api)
- [🤝 Contribution](#-contribution)

---

## 🎯 À Propos

**ClinAlert** est une plateforme complète de suivi médical intelligent qui permet aux professionnels de santé de surveiller leurs patients à distance grâce à l'intégration de montres connectées (SmartWatch).

### 🎪 Cas d'Utilisation

| Acteur | Fonctionnalités |
|--------|-----------------|
| 👨‍⚕️ **Médecin** | Suivi patients, alertes, rapports PDF, statistiques |
| 👩‍⚕️ **Infirmier** | Saisie mesures manuelles, suivi quotidien |
| 🏥 **Admin** | Gestion utilisateurs, cliniques, configuration |
| 🧑‍🤝‍🧑 **Patient** | Connexion SmartWatch, visualisation données personnelles |

---

## ✨ Fonctionnalités

### 📊 Monitoring de Santé
- ❤️ Fréquence cardiaque (BPM)
- 🫁 Saturation en oxygène (SpO2)
- 🚶 Compteur de pas quotidiens
- 😴 Suivi du sommeil
- 🌡️ Température corporelle
- 💉 Pression artérielle

### 📲 Application Mobile
- 🔗 Connexion Bluetooth BLE avec SmartWatch
- 📈 Graphiques interactifs en temps réel
- 🔔 Alertes automatiques en cas d'anomalie
- 🌍 Support multilingue (Français, English, العربية)
- 🌙 Mode sombre / Mode clair
- 📄 Génération de rapports PDF

### 🖥️ Backend API
- 🔐 Authentification JWT sécurisée
- 👥 Gestion des rôles (Admin, Doctor, Nurse, Patient)
- 📡 API REST avec 50+ endpoints
- 🗄️ Base de données PostgreSQL
- 📊 Calcul automatique des statistiques

---

## 🏗️ Architecture

### Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                    📱 Application Flutter                    │
│              (Android / iOS / Web)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                    HTTPS / REST API
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 🖥️ Backend Spring Boot                       │
│            (Controllers, Services, Security)                 │
└─────────────────────────────────────────────────────────────┘
                              │
                           JDBC
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   🗄️ PostgreSQL Database                     │
│              (Users, Patients, HealthData, Alerts)          │
└─────────────────────────────────────────────────────────────┘
```

### Stack Technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| **Frontend** | Flutter | 3.x |
| **Langage Mobile** | Dart | 3.x |
| **State Management** | Provider | Latest |
| **Backend** | Spring Boot | 3.2.0 |
| **Langage Backend** | Java | 17 LTS |
| **Database** | PostgreSQL | 15+ |
| **Authentication** | JWT (jjwt) | 0.11.5 |
| **BLE** | flutter_reactive_ble | Latest |

---

## 📱 Captures d'Écran

### 🔐 Authentification

<p align="center">
  <img src="docs/images/ecran_accueil.jpg" width="200" alt="Écran d'Accueil"/>
  <img src="docs/images/ecran_connexion.jpg" width="200" alt="Connexion"/>
  <img src="docs/images/ecran_inscription.jpg" width="200" alt="Inscription"/>
</p>

### 📊 Tableaux de Bord

<p align="center">
  <img src="docs/images/tableau_bord_medecin.jpg" width="200" alt="Dashboard Médecin"/>
  <img src="docs/images/tableau_bord_patient.jpg" width="200" alt="Dashboard Patient"/>
  <img src="docs/images/liste_patients.jpg" width="200" alt="Liste Patients"/>
</p>

### 💓 Données de Santé

<p align="center">
  <img src="docs/images/donnees_sante.jpg" width="200" alt="Données de Santé"/>
  <img src="docs/images/graphique_cardiaque.jpg" width="200" alt="Graphique Cardiaque"/>
  <img src="docs/images/historique_mesures.jpg" width="200" alt="Historique"/>
</p>

### ⌚ SmartWatch & Bluetooth

<p align="center">
  <img src="docs/images/connexion_smartwatch.jpg" width="200" alt="Connexion SmartWatch"/>
  <img src="docs/images/scan_bluetooth.jpg" width="200" alt="Scan Bluetooth"/>
  <img src="docs/images/saisie_manuelle.jpg" width="200" alt="Saisie Manuelle"/>
</p>

### ⚙️ Administration

<p align="center">
  <img src="docs/images/parametres.jpg" width="200" alt="Paramètres"/>
  <img src="docs/images/gestion_utilisateurs.jpg" width="200" alt="Gestion Utilisateurs"/>
  <img src="docs/images/gestion_cliniques.jpg" width="200" alt="Gestion Cliniques"/>
</p>

---

## 🚀 Installation

### Prérequis

- **Flutter SDK** 3.x+
- **Java JDK** 17+
- **Maven** 3.x+
- **PostgreSQL** 15+
- **Git**

### 1️⃣ Cloner le Projet

```bash
git clone https://github.com/votre-username/clinalert.git
cd clinalert
```

### 2️⃣ Backend (Spring Boot)

```bash
# Naviguer vers le backend
cd backend/doctortracker-backend

# Configurer la base de données dans application.yml
# spring.datasource.url=jdbc:postgresql://localhost:5432/clinalert

# Lancer le serveur
mvn spring-boot:run
```

Le serveur démarre sur `http://localhost:8080`

### 3️⃣ Frontend (Flutter)

```bash
# Revenir à la racine
cd ../..

# Installer les dépendances
flutter pub get

# Lancer l'application
flutter run
```

### 🐳 Docker (Optionnel)

```bash
# Backend
cd backend/doctortracker-backend
docker build -t clinalert-backend .
docker run -p 8080:8080 clinalert-backend
```

---

## ⚙️ Configuration

### Backend (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/clinalert
    username: postgres
    password: votre_mot_de_passe

app:
  jwtSecret: votre_secret_jwt_256_bits
  jwtExpirationMs: 86400000
```

### Frontend

| Fichier | Configuration |
|---------|---------------|
| `lib/services/api_service.dart` | URL du backend |
| `lib/services/ble_service.dart` | UUIDs BLE de la SmartWatch |

**URLs Backend :**
- Émulateur Android : `http://10.0.2.2:8080/api`
- Simulateur iOS : `http://localhost:8080/api`
- Appareil physique : `http://192.168.x.x:8080/api`

---

## 🔐 Sécurité

### Authentification JWT

```
┌──────────┐     POST /api/auth/login      ┌──────────────┐
│  Client  │ ────────────────────────────► │   Backend    │
│          │ ◄──────────────────────────── │              │
└──────────┘     { token, userId, role }   └──────────────┘
      │                                            │
      │        Authorization: Bearer <token>       │
      └────────────────────────────────────────────┘
```

### Matrice des Permissions

| Endpoint | ADMIN | DOCTOR | NURSE | PATIENT |
|----------|:-----:|:------:|:-----:|:-------:|
| `/api/users/*` | ✅ | ❌ | ❌ | ❌ |
| `/api/patients/*` | ✅ | ✅ | ✅ | ❌ |
| `/api/clinics/*` | ✅ | ✅ | ❌ | ❌ |
| `/api/smartwatch/*` | ✅ | ✅ | ✅ | ✅* |
| `/api/alerts/*` | ✅ | ✅ | ✅ | ✅* |

*\* Accès limité aux données personnelles*

---

## 📚 Documentation API

### Endpoints Principaux

#### 🔑 Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/login` | Connexion utilisateur |
| `POST` | `/api/auth/register` | Inscription |
| `GET` | `/api/auth/me` | Profil connecté |

#### 👥 Patients
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/patients` | Liste des patients |
| `GET` | `/api/patients/{id}` | Détails d'un patient |
| `POST` | `/api/patients` | Créer un patient |
| `PUT` | `/api/patients/{id}` | Modifier un patient |
| `DELETE` | `/api/patients/{id}` | Supprimer un patient |

#### ⌚ SmartWatch
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/smartwatch/devices` | Enregistrer un appareil |
| `POST` | `/api/smartwatch/health-data` | Soumettre des données |
| `GET` | `/api/smartwatch/health-data/{patientId}` | Historique complet |
| `GET` | `/api/smartwatch/health-data/{patientId}/stats` | Statistiques |

---

## 📁 Structure du Projet

```
clinalert/
├── 📱 lib/                          # Code Flutter
│   ├── models/                      # Modèles de données (15 classes)
│   ├── screens/                     # Écrans de l'app (27 écrans)
│   ├── services/                    # Services (API, BLE, Auth)
│   ├── providers/                   # State management (Provider)
│   ├── widgets/                     # Composants réutilisables
│   ├── themes/                      # Configuration thèmes
│   └── l10n/                        # Traductions (FR/EN/AR)
│
├── 🖥️ backend/doctortracker-backend/
│   └── src/main/java/
│       └── com/clinalert/doctortracker/
│           ├── model/               # Entités JPA (9 classes)
│           ├── repository/          # Repositories Spring Data
│           ├── service/             # Logique métier
│           ├── controller/          # Endpoints REST
│           └── security/            # Configuration JWT
│
├── 📄 docs/                         # Documentation
│   ├── images/                      # Screenshots & Diagrammes
│   ├── complete_report.tex          # Rapport technique LaTeX
│   └── frontend_documentation.tex   # Doc frontend
│
└── 📋 README.md                     # Ce fichier
```

---

## 🧪 Tests

```bash
# Tests Flutter
flutter test

# Tests Backend
cd backend/doctortracker-backend
mvn test
```

---

## 🤝 Contribution

Les contributions sont les bienvenues ! 

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

## 👥 Équipe

<p align="center">
  Développé avec ❤️ pour améliorer le suivi médical
</p>

---

<p align="center">
  <strong>ClinAlert</strong> - Système de Suivi Médical Intelligent<br/>
  <em>Healthcare Monitoring System</em>
</p>
