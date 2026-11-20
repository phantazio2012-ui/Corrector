# 📝 Correcteur de copies — application Android (APK)

Correction de copies scannées 100 % locale : OCR (Tesseract) + notation par règles, sans API.
Au **premier lancement**, l'application télécharge son moteur OCR (≈ 15–20 Mo), puis fonctionne hors ligne.

## Obtenir l'APK (sans rien installer sur votre PC)

1. Créez un dépôt GitHub (ex. `correcteur-copies`) et envoyez-y **tout le contenu de ce dossier**
   (le dossier `.github` doit être inclus, même s'il est masqué).
2. Allez dans l'onglet **Actions** du dépôt. Le workflow « Construire l'APK » démarre seul
   (sinon : *Run workflow*). Comptez 5 à 8 minutes.
3. Une fois terminé (✅), l'APK est publié dans **Releases** (colonne de droite du dépôt) sous le nom
   `correcteur-copies.apk` (aussi disponible dans *Actions → Artifacts*).
4. Ouvrez le lien de la Release sur le téléphone, téléchargez l'APK, ouvrez-le et autorisez
   « installer des applications inconnues ».

## 🔄 Mises à jour automatiques

- À chaque `push` sur `main`, GitHub reconstruit l'APK et publie une nouvelle Release `v1.0.N` (N = numéro de build).
- L'application vérifie la dernière Release au lancement (au plus toutes les 6 h, avec internet). S'il existe une
  version plus récente, un bandeau « Mise à jour disponible » propose le téléchargement ; il suffit d'ouvrir l'APK
  pour l'installer par-dessus (réglages et moteur OCR conservés).
- Réglages : case « Mises à jour automatiques » (désactivable) et bouton « Vérifier maintenant ».
- **Le dépôt doit être public** (l'application lit l'API GitHub sans identifiant). Dépôt privé = pas de vérification automatique.
- Android demande toujours une confirmation avant d'installer : une installation 100 % silencieuse n'est pas possible
  sans droits système.
- `keystore/debug.keystore` donne la même signature à chaque build, ce qui permet d'installer les mises à jour
  par-dessus l'ancienne version. Ne le supprimez pas : sinon il faudra désinstaller puis réinstaller.

## Utilisation

1. **Étape 1** (premier lancement) : choisissez la langue (Français / Anglais) et touchez
   « Télécharger le moteur OCR ».
2. Importez le **corrigé annoté** (jusqu'à 10 fichiers) puis les **copies** (jusqu'à 10 fichiers),
   depuis la galerie, les fichiers ou en prenant une photo.
3. Touchez **Corriger**. Vérifiez les lignes ⚠️ puis exportez en CSV ou imprimez en PDF.

### Format du corrigé
```
1) Capitale de la Côte d'Ivoire ? (2 pts)
Réponse : Yamoussoukro
Mots-clés : yamoussoukro
2) Résultat de 12 x 3 ? [1]
36
3) Bonne réponse du QCM ? /1
B
4) Expliquer la photosynthèse. (4 pts)
Mots-clés : lumière, chlorophylle, dioxyde de carbone, glucose
```
Points : `(2 pts)`, `[2]` ou `/2` (sans indication = 1 point). Les élèves doivent numéroter leurs réponses.

## Structure
- `www/index.html` : l'application (interface, téléchargement du moteur, OCR, notation)
- `www/sw.js` : service worker servant le moteur OCR depuis le cache (mode hors ligne)
- `scripts/copy-libs.js` : copie pdf.js et tesseract.js dans `www/lib` au moment du build
- `capacitor.config.json` : identifiant `ci.cgsi.correcteur`, nom de l'app
- `.github/workflows/build-apk.yml` : compilation automatique de l'APK + publication de la Release
- `keystore/debug.keystore` : signature fixe (mises à jour installables par-dessus)

## Personnaliser
- Nom / identifiant : `capacitor.config.json` (`appName`, `appId`).
- APK signé pour le Play Store : remplacer `assembleDebug` par `assembleRelease` et ajouter votre keystore (secrets GitHub).
- Test sur PC : `npm install && npm run libs`, puis servir `www/` avec un serveur local (`npx serve www`).
