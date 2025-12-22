# 🎯 Guide Rapide SonarQube pour ClinAlert

## ✅ Configuration Déjà Effectuée

Votre projet est maintenant configuré pour SonarQube :

### Dans pom.xml
- ✅ Propriétés SonarQube ajoutées
- ✅ Plugin JaCoCo installé (couverture de code)
- ✅ Plugin SonarQube Maven installé
- ✅ Seuil minimum de couverture : 60%

### Rapport de Couverture JaCoCo
```bash
mvn clean test jacoco:report
start target\site\jacoco\index.html
```

**Le rapport a été généré ! Consultez-le dans votre navigateur.**

---

## 🚀 Prochaines Étapes : Analyser avec SonarQube

Vous avez **2 choix** :

### Option A : SonarCloud (Cloud - RECOMMANDÉ ✨)

**Avantages** : Gratuit, pas d'installation, partage facile

**Étapes** :
1. Créer un compte sur https://sonarcloud.io
2. Connecter avec GitHub/GitLab
3. Créer une organisation
4. Générer un token : **My Account → Security → Generate Token**
5. Remplacer `votre-organisation` dans le pom.xml
6. Lancer l'analyse :

```powershell
$env:SONAR_TOKEN="votre-token-ici"

mvn clean verify sonar:sonar `
  -Dsonar.token=$env:SONAR_TOKEN
```

### Option B : SonarQube Local (Docker)

**Avantages** : Contrôle total, données privées

**Installation rapide** :
```powershell
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

Ouvrir http://localhost:9000 après 2-3 minutes
- Login: `admin` / `admin`
- Changer le mot de passe
- Créer un projet et générer un token

**Lancer l'analyse** :
```powershell
$env:SONAR_TOKEN="votre-token-local"

mvn clean verify sonar:sonar `
  -Dsonar.projectKey=clinalert_doctortracker `
  -Dsonar.host.url=http://localhost:9000 `
  -Dsonar.token=$env:SONAR_TOKEN
```

---

## 📊 Que Vais-je Voir ?

SonarQube analyse :
- 🐛 **Bugs** : Erreurs de code
- 🔐 **Vulnérabilités** : Failles de sécurité
- 💩 **Code Smells** : Mauvaises pratiques
- 📈 **Coverage** : % de code testé (actuellement visible dans JaCoCo)
- 🔁 **Duplication** : Code dupliqué

**Note de Qualité** : A (excellent) à E (très faible)

---

## 🎯 En Résumé

| Étape | Statut |
|-------|--------|
| Configuration Maven | ✅ FAIT |
| Plugin JaCoCo | ✅ FAIT |
| Plugin SonarQube | ✅ FAIT |
| Rapport Coverage Local | ✅ FAIT (voir JaCoCo) |
| Compte SonarCloud/Local | ⏳ À FAIRE |
| Token SonarQube | ⏳ À FAIRE |
| Première analyse | ⏳ À FAIRE |

---

## 💡 Recommandation

1. **Commencez par JaCoCo** : Rapport déjà ouvert dans votre navigateur
2. **Ensuite SonarCloud** : Plus simple pour débuter
3. **Corrigez les problèmes** : Améliorez progressivement

---

Pour plus de détails, consultez : `SONARQUBE_GUIDE.md`

*Guide créé le 22 Décembre 2025*
