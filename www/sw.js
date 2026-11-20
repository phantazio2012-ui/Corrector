// Service worker : sert le moteur OCR depuis le cache local (hors ligne après le 1er téléchargement).
const CACHE = 'ocr-v1';
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));
self.addEventListener('fetch', e => {
  const url = new URL(e.request.url);
  if (url.origin === self.location.origin && url.pathname.includes('/ocr/')) {
    e.respondWith(
      caches.open(CACHE).then(c => c.match(url.origin + url.pathname)).then(r => r || new Response('Moteur OCR absent', { status: 404 }))
    );
  }
});
