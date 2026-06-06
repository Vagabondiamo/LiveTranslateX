package com.livetranslatex.service

import android.app.*
import android.content.*
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.*
import android.os.*
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject lateinit var ocrEngine: OcrEngine
    @Inject lateinit var translatorEngine: TranslatorEngine

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var isCapturing = false
    private var captureReceiverRegistered = false

    companion object {
        const val CHANNEL_ID = "screen_capture_channel"
        const val ACTION_START = "com.livetranslatex.START_CAPTURE"
        const val ACTION_STOP = "com.livetranslatex.STOP_CAPTURE"
        const val ACTION_CAPTURE_ONCE = "com.livetranslatex.CAPTURE_ONCE"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val BROADCAST_TRANSLATION = "com.livetranslatex.TRANSLATION_RESULT"
    }

    private val captureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FloatingBubbleService.ACTION_CAPTURE) {
                captureOnce()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(2, buildNotification())
        registerCaptureReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA) ?: return START_NOT_STICKY
                startProjection(resultCode, resultData)
            }
            ACTION_STOP -> stopProjection()
            ACTION_CAPTURE_ONCE -> captureOnce()
        }
        return START_STICKY
    }

    private fun startProjection(resultCode: Int, resultData: Intent) {
        val mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(resultCode, resultData)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                virtualDisplay?.release()
                virtualDisplay = null
                imageReader?.close()
                imageReader = null
                isCapturing = false
            }
        }, Handler(Looper.getMainLooper()))
        setupVirtualDisplay()
        isCapturing = true
    }

    private fun setupVirtualDisplay() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "LiveTranslateX",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    fun captureOnce() {
        if (!isCapturing || imageReader == null) {
            broadcastResult("", "Cattura schermo non attiva. Avvia di nuovo Traduci Schermo.")
            return
        }

        scope.launch {
            delay(300) // attendi frame stabile
            val image = imageReader?.acquireLatestImage()
            if (image == null) {
                broadcastResult("", "Nessun frame disponibile. Tocca di nuovo la bolla.")
                return@launch
            }

            var bitmap: Bitmap? = null
            try {
                bitmap = image.toBitmap()

                val ocrResult = ocrEngine.recognizeText(bitmap)
                if (ocrResult.isNotBlank()) {
                    val translated = translatorEngine.translate(ocrResult)
                    broadcastResult(ocrResult, translated)
                } else {
                    broadcastResult("", "Nessun testo rilevato sullo schermo.")
                }
            } catch (e: Exception) {
                broadcastResult("", "Errore durante la cattura: ${e.message ?: "sconosciuto"}")
            } finally {
                bitmap?.recycle()
                image.close()
            }
        }
    }

    private fun broadcastResult(original: String, translated: String) {
        val intent = Intent(BROADCAST_TRANSLATION).setPackage(packageName).apply {
            putExtra("original", original)
            putExtra("translated", translated)
        }
        sendBroadcast(intent)
    }

    private fun stopProjection(stopService: Boolean = true) {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
        isCapturing = false
        if (stopService) stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        stopProjection(stopService = false)
        if (captureReceiverRegistered) {
            runCatching { unregisterReceiver(captureReceiver) }
            captureReceiverRegistered = false
        }
        super.onDestroy()
    }

    private fun registerCaptureReceiver() {
        val filter = IntentFilter(FloatingBubbleService.ACTION_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(captureReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(captureReceiver, filter)
        }
        captureReceiverRegistered = true
    }

    private fun Image.toBitmap(): Bitmap {
        val plane = planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val paddedWidth = width + rowPadding / pixelStride
        val paddedBitmap = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)

        buffer.rewind()
        paddedBitmap.copyPixelsFromBuffer(buffer)

        if (paddedWidth == width) return paddedBitmap

        val bitmap = Bitmap.createBitmap(paddedBitmap, 0, 0, width, height)
        paddedBitmap.recycle()
        return bitmap
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LiveTranslateX")
            .setContentText("Acquisizione schermo attiva")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
}
