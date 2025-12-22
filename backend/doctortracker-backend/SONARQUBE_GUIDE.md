# 📊 Integration de SonarQube dans ClinAlert

## 🎯 Vue d'Ensemble

SonarQube est un outil d'analyse de qualité de code qui détecte :
- 🐛 Bugs
- 🔒 Vulnérabilités de sécurité
- 💩 Code smells (mauvaises pratiques)
- 📊 Couverture de code
- 🔁 Duplication de code

---

## 🚀 Option 1 : SonarCloud (Recommandé - Cloud)

### Étape 1 : Créer un compte SonarCloud

1. Aller sur https://sonarcloud.io
2. Se connecter avec GitHub/GitLab/Bitbucket
3. Créer une nouvelle organisation
4. Créer un nouveau projet

### Étape 2 : Obtenir le token

1. Dans SonarCloud : **My Account** → **Security** → **Generate Token**
2. Copier le token généré (ex: `squ_1234567890abcdef`)

### Étape 3 : Configuration (Déjà faite dans pom.xml ✅)

Le `pom.xml` a été mis à jour avec :
- Propriétés SonarQube
- Plugin JaCoCo pour la couverture
- Plugin SonarQube Maven

**Note** : Remplacez `votre-organisation` dans le pom.xml par votre vraie organisation SonarCloud

### Étape 4 : Lancer l'analyse

```powershell
cd backend\doctortracker-backend

# Définir le token (Windows)
$env:SONAR_TOKEN="votre-token-ici"

# Lancer Tests + Couverture + SonarQube
mvn clean verify sonar:sonar `
  -Dsonar.token=$env:SONAR_TOKEN
```

### Étape 5 : Voir les résultats

Aller sur https://sonarcloud.io/organizations/votre-organisation

---

## 🏠 Option 2 : SonarQube Local (Auto-hébergé)

### Étape 1 : Installer SonarQube avec Docker

```powershell
# Créer un réseau Docker
docker network create sonarnet

# Lancer PostgreSQL pour SonarQube
docker run -d `
  --name sonarqube-db `
  --network sonarnet `
  -e POSTGRES_USER=sonar `
  -e POSTGRES_PASSWORD=sonar `
  -e POSTGRES_DB=sonarqube `
  postgres:15-alpine

# Lancer SonarQube
docker run -d `
  --name sonarqube `
  --network sonarnet `
  -p 9000:9000 `
  -e SONAR_JDBC_URL=jdbc:postgresql://sonarqube-db:5432/sonarqube `
  -e SONAR_JDBC_USERNAME=sonar `
  -e SONAR_JDBC_PASSWORD=sonar `
  sonarqube:community
```

**Attendre 2-3 minutes que SonarQube démarre**

### Étape 2 : Configuration initiale

1. Ouvrir http://localhost:9000
2. Login : `admin` / `admin`
3. Changer le mot de passe
4. Créer un nouveau projet manuellement
5. Générer un token

### Étape 3 : Modifier le pom.xml

Changer `<sonar.host.url>` :
```xml
<sonar.host.url>http://localhost:9000</sonar.host.url>
```

### Étape 4 : Lancer l'analyse

```powershell
cd backend\doctortracker-backend

# Définir le token
$env:SONAR_TOKEN="votre-token-local"

# Analyse
mvn clean verify sonar:sonar `
  -Dsonar.projectKey=clinalert_doctortracker `
  -Dsonar.host.url=http://localhost:9000 `
  -Dsonar.token=$env:SONAR_TOKEN
```

### Étape 5 : Voir les résultats

http://localhost:9000/dashboard?id=clinalert_doctortracker

---

## 📊 Commandes Utiles

### Juste la couverture de code (sans SonarQube)
```powershell
mvn clean test jacoco:report
# Rapport : target/site/jacoco/index.html
```

### Tests + Couverture + SonarCloud
```powershell
mvn clean verify sonar:sonar
```

### Forcer une nouvelle analyse
```powershell
mvn sonar:sonar -Dsonar.token=$env:SONAR_TOKEN
```

### Vérifier la couverture minimum (60%)
```powershell
mvn clean test jacoco:check
```

---

## 🔧 Alternatives Sans Installation

### GitHub Actions (Automatique sur chaque push)

Créer `.github/workflows/sonarcloud.yml` :

```yaml
name: SonarCloud Analysis
on:
  push:
    branches:
      - main
      - develop
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarcloud:
    name: SonarCloud
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build and analyze
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          cd backend/doctortracker-backend
          mvn clean verify sonar:sonar \
            -Dsonar.projectKey=clinalert_doctortracker \
            -Dsonar.organization=votre-org \
            -Dsonar.host.url=https://sonarcloud.io
```

---

## 📈 Ce que vous verrez dans SonarQube

### Métriques Principales
- **Bugs** : Erreurs de code
- **Vulnerabilities** : Failles de sécurité
- **Code Smells** : Mauvaises pratiques
- **Coverage** : % de code testé
- **Duplications** : Code dupliqué
- **Security Hotspots** : Points sensibles

### Note de Qualité
- **A** : Excellent (0-5%)
- **B** : Bon (6-10%)
- **C** : Moyen (11-20%)
- **D** : Faible (21-50%)
- **E** : Très faible (>50%)

---

## 🎯 Prochaines Étapes

1. ✅ Configuration Maven (FAIT)
2. ⏳ Créer compte SonarCloud OU installer local
3. ⏳ Générer token
4. ⏳ Lancer première analyse
5. ⏳ Corriger les problèmes détectés
6. ⏳ Intégrer dans CI/CD (optionnel)

---

## 🔍 Vérification Rapide

Pour tester localement sans SonarQube :

```powershell
# Génération du rapport JaCoCo uniquement
mvn clean test jacoco:report

# Ouvrir le rapport
start target\site\jacoco\index.html
```

Vous verrez votre **couverture actuelle : probablement 20-30%**

---

## 💡 Conseils

1. **Commencez par SonarCloud** - Plus simple
2. **Fixez d'abord les bugs critiques** - Puis les vulnérabilités
3. **Visez 60%+ de couverture** - Puis augmentez progressivement
4. **Intégrez dans CI/CD** - Automatisez l'analyse

---

*Guide créé le 22 Décembre 2025*
