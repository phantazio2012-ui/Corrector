// Copie les bibliothèques JS (PDF + OCR) dans www/lib pour qu'elles soient embarquées dans l'APK.
// Le moteur OCR lui-même (worker, noyau WASM, langues) est téléchargé par l'application au 1er lancement.
const fs = require('fs'), path = require('path');
const out = path.join(__dirname, '..', 'www', 'lib');
fs.mkdirSync(out, { recursive: true });
const nm = path.join(__dirname, '..', 'node_modules');
const files = [
  ['pdfjs-dist/build/pdf.min.js', 'pdf.min.js'],
  ['pdfjs-dist/build/pdf.worker.min.js', 'pdf.worker.min.js'],
  ['tesseract.js/dist/tesseract.min.js', 'tesseract.min.js'],
];
for (const [src, dst] of files) {
  fs.copyFileSync(path.join(nm, src), path.join(out, dst));
  console.log('copié', dst);
}
