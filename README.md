# LiveTranslateX

App Android Kotlin per traduzione in tempo reale tramite OCR + overlay.

## Funzionalità

- 📱 **Traduci Schermo** — overlay su qualsiasi app tramite MediaProjection
- 📷 **Fotocamera** — traduzione live a 10 FPS con CameraX
- 📚 **Manga/Webtoon** — lettore integrato con OCR + inpainting
- 🖼️ **Galleria** — importa immagini e traduci
- 📝 **Cronologia** — storico traduttivo in Room DB

## Motori OCR

| Motore | Lingue | Uso |
|--------|--------|-----|
| ML Kit | EN/IT/FR/ES/JA/ZH/KO | Default |
| PaddleOCR | JA/ZH/KO (manga) | Avanzato (TODO) |

## Traduttori

| Traduttore | Tipo | Note |
|-----------|------|------|
| ML Kit | Offline | Default, gratuito |
| DeepL | Online | Richiede API key |
| OpenAI | AI | Contestuale, richiede API key |

## Build locale

```bash
# Clone
git clone https://github.com/TUO_USERNAME/LiveTranslateX.git
cd LiveTranslateX

# API keys (opzionale)
echo "DEEPL_API_KEY=la_tua_chiave" >> local.properties
echo "OPENAI_API_KEY=la_tua_chiave" >> local.properties

# Build debug APK
./gradlew assembleDebug
```

APK in: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

GitHub Actions costruisce automaticamente:
- **Debug APK** su ogni push/PR
- **Release APK** su push a `main`

Secrets richiesti in GitHub → Settings → Secrets:
- `DEEPL_API_KEY`
- `OPENAI_API_KEY`

## Requisiti

- Android 8.0+ (API 26)
- Permessi: Camera, Overlay, MediaProjection
- JDK 17

## Struttura

```
app/src/main/java/com/livetranslatex/
├── data/          # OCR, traduttori, Room, repository
├── domain/        # Modelli, use cases
├── presentation/  # Schermate Compose + ViewModels
├── service/       # ScreenCapture, Overlay, Translation
├── util/          # Estensioni (md5, ecc.)
└── di/            # Hilt modules
```
