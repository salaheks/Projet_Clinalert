# README - JMeter Performance Tests

## 📊 Structure JMeter

```
src/test/jmeter/
├── plans/                    # Fich iers .jmx (plans de test)
│   └── login_load_test.jmx  # Login 50 users
├── data/                     # Données CSV pour paramétrage
│   ├── doctors.csv           # 7 comptes doctors
│   ├── patients.csv          # 10 patients test
│   └── clinics.csv          # 5 cliniques test
├── scripts/                  # Scripts helper (Groovy/BeanShell)
└── reports/                 # Rapports générés (.gitignored)
```

## 🚀 Exécution

### Mode GUI (Pour visualiser et éditer les tests)
Utilisez le plugin Maven pour lancer l'interface graphique avec la configuration correcte. 
**Note :** Il faut exécuter `configure` avant `gui` pour préparer l'environnement.

```bash
cd backend/doctortracker-backend
mvn jmeter:configure jmeter:gui "-Dsurefire.skip=true"
```
*Note : Cela téléchargera et lancera la bonne version de JMeter.*

### Mode CLI (Production)
```bash
# Exécuter tous les tests
mvn clean integration-test

# Ou spécifiquement JMeter
mvn jmeter:jmeter

# Générer rapports HTML
mvn jmeter:results

# Voir rapports
# Fichier: target/jmeter/reports/index.html
```

## 📝 Plans de Test Disponibles

1. **crud_performance.jmx**
   - **Scénario Complet** : Login -> Get Patients -> Create Patient -> Get Clinics
   - **Utilisateurs** : 30 threads concurrents
   - **Durée** : Configurable via `-Djmeter.test.duration=...` (défaut : 300s)
   - **Données** : Utilise `doctors.csv` et `patients.csv` du dossier `data/`

## 🎯 Seuils de Performance

| Métrique | Seuil |
|----------|-------|
| Avg Response Time | < 500ms |
| 90th Percentile | < 800ms |
| Throughput | > 50 req/s |
| Error Rate | < 1% |

## 📦 Prérequis

- ✅ Spring Boot backend démarré (localhost:8080)
- ✅ Java 17+ installé
- ✅ JAVA_HOME configuré
- ✅ JMeter 5.6.3+ installé (pour mode GUI)
