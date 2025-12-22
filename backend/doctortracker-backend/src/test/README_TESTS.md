# 🧪 Suite de Tests JUnit - ClinAlert

## 📋 Vue d'Ensemble

Cette suite de tests complète couvre l'ensemble du backend Spring Boot de ClinAlert avec **38+ tests** répartis en 3 catégories :

| Type de Test | Description | Nombre de fichiers | Tests |
|--------------|-------------|-------------------|-------|
| **Tests Unitaires** | Services isolés avec mocks | 2 | 18 |
| **Tests d'Intégration** | Controllers avec contexte Spring | 1 | 10 |
| **Tests Repository** | Requêtes JPA avec H2 | 1 | 10 |

---

## 🗂️ Structure des Tests

```
src/test/
├── java/com/clinalert/doctortracker/
│   ├── service/
│   │   ├── AuthServiceTest.java          (8 tests)  ✅
│   │   └── PatientServiceTest.java       (10 tests) ✅
│   ├── controller/
│   │   └── PatientControllerTest.java    (10 tests) ✅
│   └── repository/
│       └── UserRepositoryTest.java       (10 tests) ✅
└── resources/
    └── application-test.yml               (Configuration H2)
```

---

## 🚀 Exécution des Tests

### Tous les tests
```bash
cd backend/doctortracker-backend
mvn test
```

### Une classe de test spécifique
```bash
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=PatientControllerTest
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=PatientServiceTest
```

### Un test individuel
```bash
mvn test -Dtest=AuthServiceTest#login_WithValidCredentials_ShouldReturnLoginResponse
```

### Tests en mode silencieux (sans logs détaillés)
```bash
mvn test -q
```

### Tests avec rapport de couverture JaCoCo
```bash
mvn test jacoco:report
# Le rapport sera dans target/site/jacoco/index.html
```

---

## 📊 Détails des Tests

### 1️⃣ AuthServiceTest (Tests Unitaires)

**Fichier** : `service/AuthServiceTest.java`

**Tests couverts** :
- ✅ Login réussi avec identifiants valides
- ✅ Login échoué avec mot de passe incorrect
- ✅ Login échoué avec email inexistant
- ✅ Inscription réussie d'un docteur
- ✅ Inscription réussie d'un patient
- ✅ Inscription échouée - email déjà utilisé
- ✅ Récupération de l'utilisateur courant (authentifié)
- ✅ Récupération de l'utilisateur courant (non authentifié)

**Techniques utilisées** :
- Mockito pour simuler les dépendances
- Pattern AAA (Arrange-Act-Assert)
- Vérification des appels de méthodes avec `verify()`

---

### 2️⃣ PatientServiceTest (Tests Unitaires)

**Fichier** : `service/PatientServiceTest.java`

**Tests couverts** :
- ✅ Récupérer tous les patients
- ✅ Récupérer un patient par ID (trouvé)
- ✅ Récupérer un patient par ID (non trouvé)
- ✅ Récupérer les patients d'un docteur
- ✅ Récupérer les patients d'une clinique
- ✅ Créer un nouveau patient
- ✅ Supprimer un patient
- ✅ Mettre à jour le statut d'un patient (succès)
- ✅ Mettre à jour le statut d'un patient (patient inexistant)
- ✅ Gérer une liste vide de patients

---

### 3️⃣ PatientControllerTest (Tests d'Intégration)

**Fichier** : `controller/PatientControllerTest.java`

**Tests couverts** :
- ✅ GET /api/patients - Liste complète
- ✅ GET /api/patients/{id} - Détails d'un patient
- ✅ GET /api/patients/{id} - Patient inexistant (404)
- ✅ GET /api/patients/doctor/{doctorId} - Patients d'un docteur
- ✅ POST /api/patients - Créer un nouveau patient
- ✅ PUT /api/patients/{id} - Mettre à jour un patient
- ✅ PUT /api/patients/{id}/status - Changer le statut
- ✅ DELETE /api/patients/{id} - Supprimer un patient
- ✅ GET /api/patients - Sans authentification (401)
- ✅ GET /api/patients - Avec rôle PATIENT (test de permissions)

**Techniques utilisées** :
- MockMvc pour simuler les requêtes HTTP
- JSONPath pour valider la structure JSON
- @WithMockUser pour simuler l'authentification

---

### 4️⃣ UserRepositoryTest (Tests Repository)

**Fichier** : `repository/UserRepositoryTest.java`

**Tests couverts** :
- ✅ Rechercher un utilisateur par email (trouvé)
- ✅ Rechercher un utilisateur par email (non trouvé)
- ✅ Sensibilité à la casse de la recherche
- ✅ Vérifier l'existence d'un email (existe)
- ✅ Vérifier l'existence d'un email (n'existe pas)
- ✅ Sauvegarder un nouvel utilisateur
- ✅ Mettre à jour un utilisateur existant
- ✅ Violation de contrainte unique (email dupliqué)
- ✅ Compter le nombre total d'utilisateurs
- ✅ Supprimer un utilisateur

**Techniques utilisées** :
- Base H2 en mémoire
- TestEntityManager pour préparer les données
- Test des contraintes de base de données

---

## 🔧 Configuration de Test

### Base de Données H2

Les tests utilisent H2 en mémoire (pas besoin de PostgreSQL) :

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

**Avantages** :
- Rapide (en mémoire)
- Isolé (chaque test a une DB vierge)
- Pas de configuration externe nécessaire

---

## 📖 Comprendre la Structure des Tests

### Pattern AAA (Arrange-Act-Assert)

Tous les tests suivent ce pattern :

```java
@Test
void testName() {
    // ===== ARRANGE (Préparer) =====
    // Configurer les mocks et les données de test
    when(repository.findById("id")).thenReturn(Optional.of(entity));
    
    // ===== ACT (Agir) =====
    // Exécuter la méthode à tester
    Result result = service.method(parameter);
    
    // ===== ASSERT (Vérifier) =====
    // Vérifier que le résultat est correct
    assertEquals(expected, result);
    verify(repository).findById("id");
}
```

### Annotations Importantes

| Annotation | Utilisation |
|------------|-------------|
| `@ExtendWith(MockitoExtension.class)` | Active Mockito pour les tests unitaires |
| `@SpringBootTest` | Charge le contexte Spring complet |
| `@AutoConfigureMockMvc` | Configure MockMvc pour tester les controllers |
| `@DataJpaTest` | Configure un contexte JPA minimal avec H2 |
| `@Mock` | Crée un mock d'une dépendance |
| `@InjectMocks` | Injecte les mocks dans la classe testée |
| `@MockBean` | Remplace un bean Spring par un mock |
| `@WithMockUser` | Simule un utilisateur authentifié |

---

## 🎯 Bonnes Pratiques Appliquées

1. **Isolation** : Chaque test est indépendant
2. **Clarté** : Noms de tests descriptifs en français
3. **Documentation** : Commentaires détaillés expliquant chaque test
4. **Couverture** : Cas normaux ET cas d'erreur
5. **Rapidité** : Tests rapides grâce à H2 et aux mocks
6. **Maintenabilité** : Code propre et bien structuré

---

## 🛠️ Ajouter de Nouveaux Tests

### 1. Test Unitaire (Service)

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void testMethod() {
        // Arrange, Act, Assert
    }
}
```

### 2. Test d'Intégration (Controller)

```java
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MyService service;
    
    @Test
    @WithMockUser(roles = "DOCTOR")
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
               .andExpect(status().isOk());
    }
}
```

### 3. Test Repository

```java
@DataJpaTest
class MyRepositoryTest {
    @Autowired
    private MyRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void testQuery() {
        // Persister des données avec entityManager
        // Tester les requêtes du repository
    }
}
```

---

## 📈 Rapport de Couverture

Pour générer un rapport de couverture avec JaCoCo :

```bash
mvn clean test jacoco:report
```

Le rapport HTML sera généré dans :
```
target/site/jacoco/index.html
```

**Objectif de couverture recommandé** : 80%+

---

## 🐛 Debugging des Tests

### Afficher les logs SQL
Les requêtes SQL sont déjà activées dans `application-test.yml` :

```yaml
spring:
  jpa:
    show-sql: true
```

### Afficher les logs Spring Security
Pour déboguer l'authentification :

```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

### Mode Debug IntelliJ/VSCode
1. Placer un breakpoint dans le test
2. Clic droit → "Debug Test"
3. Inspecter les variables et l'exécution

---

## ✅ Vérification Rapide

Pour vérifier que tout fonctionne :

```bash
# Naviguer vers le backend
cd backend/doctortracker-backend

# Exécuter tous les tests
mvn test

# Résultat attendu :
# Tests run: 38, Failures: 0, Errors: 0, Skipped: 0
```

Si des tests échouent, vérifiez :
1. ✅ Java 17 est installé (`java -version`)
2. ✅ Maven est installé (`mvn -version`)
3. ✅ Les dépendances sont téléchargées (`mvn clean install`)

---

## 📚 Ressources Additionnelles

- **JUnit 5** : https://junit.org/junit5/docs/current/user-guide/
- **Mockito** : https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Boot Testing** : https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- **AssertJ** : https://assertj.github.io/doc/

---

## 👨‍💻 Auteur

**ClinAlert Team** - Système de Suivi Médical Intelligent

*Tests créés avec ❤️ pour garantir la qualité du code*
