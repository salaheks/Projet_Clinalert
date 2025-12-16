# FAQ - Foire Aux Questions (PAQ)
# Projet Clinalert & DoctorTracker

## Table des Matières
1. [Questions Générales](#questions-générales)
2. [Installation et Configuration](#installation-et-configuration)
3. [Utilisation de l'Application Mobile](#utilisation-de-lapplication-mobile)
4. [Backend DoctorTracker](#backend-doctortracker)
5. [Sécurité et Confidentialité](#sécurité-et-confidentialité)
6. [Bluetooth Low Energy (BLE)](#bluetooth-low-energy-ble)
7. [Dépannage](#dépannage)
8. [Développement et Contribution](#développement-et-contribution)

---

## Questions Générales

### Qu'est-ce que Clinalert ?
Clinalert est une application mobile développée avec Flutter qui permet aux patients de se connecter à une montre connectée (SmartWatch) via Bluetooth Low Energy (BLE) pour visualiser leurs données de santé en temps réel et les transmettre à leur médecin.

### Qu'est-ce que DoctorTracker ?
DoctorTracker est le backend du système, développé avec Spring Boot. Il reçoit, stocke et gère les données de santé des patients envoyées depuis l'application mobile Clinalert.

### Quelles sont les fonctionnalités principales ?
- Connexion Bluetooth avec une montre connectée
- Visualisation des données de santé en temps réel (rythme cardiaque, tension artérielle, etc.)
- Stockage local sécurisé des mesures
- Envoi des données au médecin via le backend
- Système d'alertes pour les valeurs anormales
- Historique des mesures
- Gestion du consentement patient

### Quelles plateformes sont supportées ?
L'application mobile Clinalert supporte :
- Android
- iOS
- Linux
- macOS
- Windows (expérimental)
- Web (limité pour les fonctions BLE)

---

## Installation et Configuration

### Quels sont les prérequis pour l'application mobile ?
- **Flutter SDK** : Version 3.x ou supérieure (actuellement 3.9.2+)
- Un appareil ou émulateur Android/iOS
- Pour iOS : Xcode et CocoaPods
- Pour Android : Android Studio et SDK

### Quels sont les prérequis pour le backend ?
- **Java JDK** : Version 17 ou supérieure
- **Maven** : Version 3.x ou supérieure
- **PostgreSQL** : Version 14 ou supérieure (ou utiliser Docker)

### Comment installer l'application mobile ?
```bash
# Cloner le dépôt
git clone [URL_DU_DEPOT]
cd Projet_Clinalert

# Installer les dépendances
flutter pub get

# Lancer l'application
flutter run
```

### Comment configurer le backend ?
1. Naviguez vers `backend/doctortracker-backend`
2. Configurez `src/main/resources/application.yml` :
   - URL de la base de données PostgreSQL
   - `app.jwtSecret` : Clé secrète pour JWT
   - `app.hmacSecret` : Clé secrète pour la signature HMAC-SHA256
3. Lancez le backend :
```bash
mvn spring-boot:run
```

### Comment configurer l'URL du backend dans l'application ?
Éditez le fichier `lib/services/api_service.dart` et mettez à jour `_baseUrl` :
- **Émulateur Android** : `http://10.0.2.2:8080/api`
- **Simulateur iOS** : `http://localhost:8080/api`
- **Appareil physique** : `http://[ADRESSE_IP_LOCALE]:8080/api` (ex: `http://192.168.1.10:8080/api`)

### Comment configurer les UUID BLE de ma montre ?
Ouvrez `lib/services/ble_service.dart` et remplacez :
- `_serviceUuid` : UUID du service BLE de votre appareil
- `_charUuid` : UUID de la caractéristique BLE de votre appareil

---

## Utilisation de l'Application Mobile

### Comment se connecter à l'application ?
Actuellement, l'authentification est simulée pour les tests. Entrez n'importe quel identifiant et mot de passe pour accéder au tableau de bord. La version de production utilisera une authentification JWT sécurisée.

### Comment scanner et connecter une montre connectée ?
1. Allez sur le **Tableau de bord**
2. Appuyez sur **"Scan Device"** (Rechercher un appareil)
3. Autorisez les permissions Bluetooth si demandé
4. Sélectionnez votre appareil dans la liste
5. Attendez la connexion (l'icône de statut passera au vert)

### Comment visualiser mes données de santé ?
Une fois connecté à votre montre :
- Les données s'affichent automatiquement en temps réel
- Naviguez vers l'écran des mesures pour voir les détails
- Les données sont automatiquement sauvegardées localement

### Comment envoyer mes données à mon médecin ?
1. Allez sur le **Tableau de bord**
2. Appuyez sur **"Send to Doctor"** (Envoyer au médecin)
3. Sélectionnez les mesures à envoyer
4. Confirmez l'envoi
5. L'application signera les données avec HMAC-SHA256 et les enverra au backend

### Mes données sont-elles sauvegardées si je perds la connexion ?
Oui, toutes les mesures sont automatiquement sauvegardées localement dans une base de données Hive chiffrée. Vous pouvez les envoyer plus tard même hors ligne.

### Comment gérer mon consentement ?
L'application vérifie le consentement de l'utilisateur avant de lire les données BLE. Vous pouvez gérer vos préférences de consentement dans les paramètres de l'application.

---

## Backend DoctorTracker

### Sur quel port le backend écoute-t-il ?
Le backend écoute par défaut sur le port **8080**. Vous pouvez y accéder via `http://localhost:8080`.

### Comment tester le backend sans l'application mobile ?
Utilisez cURL ou Postman. Exemple avec cURL :
```bash
# Calculez d'abord le HMAC-SHA256 du payload JSON avec votre clé secrète
curl -X POST http://localhost:8080/api/measurements \
  -H "Content-Type: application/json" \
  -H "X-Signature: [SIGNATURE_HMAC_HEX]" \
  -d '[{"id":"1","patientId":"p1","value":100.0,"type":"heart_rate"}]'
```

### Comment configurer la base de données PostgreSQL ?
Par défaut, le backend se connecte à `jdbc:postgresql://localhost:5432/doctortracker`.
1. Créez une base de données nommée `doctortracker`
2. Configurez les identifiants dans `application.yml`
3. Les tables seront créées automatiquement au premier lancement (grâce à Hibernate)

### Puis-je utiliser Docker pour le backend ?
Oui, un `Dockerfile` est fourni :
```bash
docker build -t doctortracker-backend .
docker run -p 8080:8080 doctortracker-backend
```

### Quels endpoints API sont disponibles ?
Principaux endpoints :
- `POST /api/measurements` : Recevoir des mesures de santé
- `GET /api/patients` : Lister les patients
- `GET /api/patients/{id}` : Obtenir les détails d'un patient
- `GET /api/alerts` : Lister les alertes
- `GET /api/doctors` : Lister les médecins

---

## Sécurité et Confidentialité

### Comment les données sont-elles sécurisées ?
Le système utilise plusieurs couches de sécurité :
- **Chiffrement local** : Les données sont chiffrées avec AES via Hive
- **HMAC-SHA256** : Intégrité et authenticité des données envoyées
- **JWT** : Authentification sécurisée (en production)
- **HTTPS** : Communication chiffrée (recommandé en production)
- **Stockage sécurisé** : Utilisation de `flutter_secure_storage` pour les secrets

### Qu'est-ce que HMAC et pourquoi est-il utilisé ?
HMAC (Hash-based Message Authentication Code) garantit que :
1. Les données n'ont pas été modifiées en transit (intégrité)
2. Les données proviennent bien de l'application autorisée (authenticité)

L'application calcule un hash des données avec une clé secrète partagée. Le backend vérifie ce hash avec la même clé.

### Comment sont gérées les clés secrètes ?
- **Application mobile** : Stockage sécurisé via `flutter_secure_storage`
- **Backend** : Configuration dans `application.yml` (variables d'environnement recommandées en production)
- **Important** : Les clés HMAC doivent être identiques côté application et backend

### Le consentement du patient est-il respecté ?
Oui, l'application vérifie le consentement avant de lire les données BLE. Les préférences de consentement sont stockées et respectées tout au long de l'utilisation.

### Les données sont-elles anonymisées ?
Les données sont associées à un `patientId` pour le suivi médical. Pour l'anonymisation complète, des fonctionnalités supplémentaires peuvent être implémentées selon les exigences RGPD.

---

## Bluetooth Low Energy (BLE)

### Quelles permissions sont nécessaires pour BLE ?
- **Android** : BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION
- **iOS** : Bluetooth et descriptions dans Info.plist

### Pourquoi la localisation est-elle requise sur Android ?
Android exige la permission de localisation pour scanner les appareils BLE, même si l'application n'utilise pas réellement la géolocalisation. C'est une exigence de sécurité d'Android.

### Mon appareil BLE n'apparaît pas lors du scan. Que faire ?
Vérifiez :
1. Le Bluetooth est activé sur votre appareil mobile
2. L'appareil BLE est allumé et en mode appariement
3. Les permissions Bluetooth et localisation sont accordées
4. Vous êtes à proximité de l'appareil (< 10 mètres généralement)
5. L'appareil n'est pas déjà connecté à un autre appareil

### Comment identifier les UUID de mon appareil BLE ?
Utilisez une application de scan BLE comme :
- **Android** : nRF Connect for Mobile
- **iOS** : LightBlue Explorer

Ces applications vous montreront les services et caractéristiques disponibles avec leurs UUID.

### Puis-je connecter plusieurs appareils BLE simultanément ?
L'implémentation actuelle se concentre sur une connexion unique. Le support multi-appareils nécessiterait des modifications dans `ble_service.dart`.

---

## Dépannage

### L'application ne se lance pas. Que faire ?
1. Vérifiez que Flutter est correctement installé : `flutter doctor`
2. Installez les dépendances : `flutter pub get`
3. Nettoyez le projet : `flutter clean`
4. Relancez : `flutter run`

### Erreur "Null Safety" lors de la compilation
Le projet utilise Dart Null Safety. Assurez-vous d'utiliser Flutter 3.x+ et que toutes les dépendances sont compatibles.

### Le backend ne démarre pas
Vérifiez :
1. Java 17+ est installé : `java -version`
2. PostgreSQL est en cours d'exécution
3. La base de données `doctortracker` existe
4. Les identifiants dans `application.yml` sont corrects
5. Le port 8080 n'est pas déjà utilisé

### Erreur "HMAC signature verification failed"
- Assurez-vous que `app.hmacSecret` dans le backend correspond exactement à la clé utilisée dans l'application mobile
- Vérifiez que le payload JSON est formaté correctement (même ordre, pas d'espaces supplémentaires)

### L'application ne peut pas se connecter au backend
1. **Émulateur Android** : Utilisez `http://10.0.2.2:8080/api` au lieu de `localhost`
2. **Appareil physique** : Utilisez l'adresse IP locale de votre ordinateur
3. Vérifiez que le pare-feu n'en bloque pas l'accès
4. Vérifiez que le backend est bien en cours d'exécution

### Impossible de se connecter à l'appareil BLE
1. Redémarrez le Bluetooth sur votre appareil mobile
2. Redémarrez l'appareil BLE
3. Vérifiez que les UUID dans `ble_service.dart` sont corrects
4. Sur iOS, vérifiez les descriptions de permission dans Info.plist
5. Réinstallez l'application si les permissions posent problème

### Les données ne sont pas sauvegardées localement
Vérifiez :
1. Les permissions de stockage sont accordées
2. Hive est correctement initialisé dans `main.dart`
3. Consultez les logs pour les erreurs de base de données

---

## Développement et Contribution

### Comment contribuer au projet ?
1. Forkez le dépôt
2. Créez une branche pour votre fonctionnalité : `git checkout -b feature/nouvelle-fonctionnalite`
3. Committez vos changements : `git commit -m "Ajout d'une nouvelle fonctionnalité"`
4. Poussez vers la branche : `git push origin feature/nouvelle-fonctionnalite`
5. Ouvrez une Pull Request

### Quelles sont les conventions de code ?
Le projet suit les conventions définies dans le PAQP (Plan d'Assurance Qualité Projet) :
- **Nommage** : Noms explicites en anglais
- **Largeur de ligne** : Maximum 80-120 caractères
- **Commentaires** : Obligatoires pour les sections complexes
- **Null Safety** : Gestion stricte des références nulles
- **Constantes** : Pas de "nombres magiques", utiliser des constantes nommées

### Comment exécuter les tests ?
```bash
# Tests Flutter
flutter test

# Tests backend (Maven)
cd backend/doctortracker-backend
mvn test
```

### Comment linter le code ?
```bash
# Analyse Flutter
flutter analyze

# Formatage Flutter
flutter format .
```

### Où trouver la documentation complète ?
- **README.md** : Instructions d'installation et d'utilisation
- **PAQP.tex** : Plan d'Assurance Qualité Projet (dans `/docs`)
- **FAQ.md** : Ce document (Foire Aux Questions)
- Code source : Commentaires dans les fichiers

### Comment rapporter un bug ?
Ouvrez une issue sur le dépôt GitHub avec :
- Description claire du problème
- Étapes pour reproduire
- Comportement attendu vs comportement actuel
- Captures d'écran si applicable
- Version de Flutter/Java et système d'exploitation

### Quelles dépendances sont utilisées ?
**Application mobile (Flutter)** :
- `provider` : Gestion d'état
- `go_router` : Navigation
- `flutter_reactive_ble` : Communication BLE
- `hive` : Base de données locale
- `dio` : Client HTTP
- `crypto` : Cryptographie (HMAC)
- `flutter_secure_storage` : Stockage sécurisé

**Backend (Spring Boot)** :
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Spring Boot Security (pour JWT)

---

## Support et Contact

### Où obtenir de l'aide ?
- Consultez d'abord cette FAQ
- Lisez la documentation dans `/docs`
- Ouvrez une issue sur GitHub pour les bugs
- Contactez l'équipe de développement via le dépôt GitHub

### Le projet est-il open source ?
Consultez le fichier LICENSE dans le dépôt pour connaître les termes de licence du projet.

---

**Dernière mise à jour** : Décembre 2024  
**Version** : 1.0.0

---

*Cette FAQ sera mise à jour régulièrement au fur et à mesure de l'évolution du projet. N'hésitez pas à suggérer des améliorations ou des questions supplémentaires.*
