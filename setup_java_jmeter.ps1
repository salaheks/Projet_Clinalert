# Script PowerShell : Configuration JAVA_HOME pour JMeter
# Fix: "Not able to find Java executable or version"

Write-Host "=== Configuration JAVA_HOME pour JMeter ===" -ForegroundColor Cyan

# 1. Vérifier si Java est installé
Write-Host "`n[1/4] Vérification installation Java..." -ForegroundColor Yellow
$javaExe = Get-Command java -ErrorAction SilentlyContinue

if ($null -eq $javaExe) {
    Write-Host "❌ Java n'est PAS installé sur ce système!" -ForegroundColor Red
    Write-Host "Téléchargez Java JDK 17 depuis: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Java trouvé: $($javaExe.Path)" -ForegroundColor Green

# 2. Extraire JAVA_HOME depuis le path de java.exe
Write-Host "`n[2/4] Détermination JAVA_HOME..." -ForegroundColor Yellow
$javaPath = $javaExe.Path
# java.exe → bin → JAVA_HOME
$javaHome = Split-Path (Split-Path $javaPath -Parent) -Parent

if ($javaHome -match "javapath") {
    # Si chemin symbolique javapath, trouver le vrai répertoire
    $realJavaPath = (Get-Item $javaPath).Target
    if ($realJavaPath) {
        $javaHome = Split-Path (Split-Path $realJavaPath -Parent) -Parent
    }
}

Write-Host "JAVA_HOME détecté: $javaHome" -ForegroundColor Cyan

# 3. Vérifier version Java
Write-Host "`n[3/4] Vérification version Java..." -ForegroundColor Yellow
$javaVersion = & java -version 2>&1 | Select-String "version" | Select-Object -First 1
Write-Host "$javaVersion" -ForegroundColor Green

# 4. Configurer variable d'environnement JAVA_HOME (USER)
Write-Host "`n[4/4] Configuration JAVA_HOME..." -ForegroundColor Yellow

try {
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')
    Write-Host "✅ JAVA_HOME configuré: $javaHome" -ForegroundColor Green
    
    # Rafraîchir la session PowerShell
    $env:JAVA_HOME = $javaHome
    Write-Host "✅ Variable JAVA_HOME mise à jour pour cette session" -ForegroundColor Green
    
    Write-Host "`n=== Configuration TERMINÉE ===" -ForegroundColor Cyan
    Write-Host "`nℹ️  IMPORTANT:" -ForegroundColor Yellow
    Write-Host "   1. FERMEZ cette fenêtre PowerShell" -ForegroundColor White
    Write-Host "   2. Ouvrez une NOUVELLE fenêtre PowerShell" -ForegroundColor White
    Write-Host "   3. Testez: jmeter --version" -ForegroundColor White
    
} catch {
    Write-Host "❌ Erreur lors de la configuration: $_" -ForegroundColor Red
    exit 1
}

# Afficher récapitulatif
Write-Host "`n📋 Récapitulatif:" -ForegroundColor Cyan
Write-Host "   JAVA_HOME = $javaHome" -ForegroundColor White
Write-Host "   Java Path = $javaPath" -ForegroundColor White
