// Copie les bibliothèques et le moteur OCR dans www/ pour qu'ils soient EMBARQUÉS dans l'APK (aucun téléchargement au 1er lancement).
const fs = require('fs'), path = require('path');
const root = path.join(__dirname, '..');
const nm = path.join(root, 'node_modules');
const files = [
  ['pdfjs-dist/build/pdf.min.js', 'www/lib/pdf.min.js'],
  ['pdfjs-dist/build/pdf.worker.min.js', 'www/lib/pdf.worker.min.js'],
  ['tesseract.js/dist/tesseract.min.js', 'www/lib/tesseract.min.js'],
  ['tesseract.js/dist/worker.min.js', 'www/ocr/worker.min.js'],
  ['tesseract.js-core/tesseract-core-simd-lstm.wasm.js', 'www/ocr/core/tesseract-core-simd-lstm.wasm.js'],
  ['tesseract.js-core/tesseract-core-lstm.wasm.js', 'www/ocr/core/tesseract-core-lstm.wasm.js'],
  ['@tesseract.js-data/fra/4.0.0_best_int/fra.traineddata.gz', 'www/ocr/lang/fra.traineddata.gz'],
  ['@tesseract.js-data/eng/4.0.0_best_int/eng.traineddata.gz', 'www/ocr/lang/eng.traineddata.gz'],
];
for (const [src, dst] of files) {
  const to = path.join(root, dst);
  fs.mkdirSync(path.dirname(to), { recursive: true });
  fs.copyFileSync(path.join(nm, src), to);
  console.log('copié', dst, fs.statSync(to).size, 'octets');
}
