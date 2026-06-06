package com.livetranslatex.service

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.livetranslatex.R
import com.livetranslatex.data.ocr.OcrEngine
import com.livetranslatex.data.translator.TranslatorEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class FloatingBubbleService : Service() {

    @Inject lateinit var ocrEngine: OcrEngine
    @Inject lateinit var translatorEngine: TranslatorEngine

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: View
    private lateinit var overlayView: View
    private var overlayVisible = false
    private var translationReceiverRegistered = false

    companion object {
        const val CHANNEL_ID = "bubble_channel"
        const val ACTION_STOP = "com.livetranslatex.STOP_BUBBLE"
        const val ACTION_CAPTURE = "com.livetranslatex.CAPTURE"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, buildNotification())
        inflateBubble()
        inflateOverlay()
        registerTranslationReceiver()
    }

    // ── Bubble ────────────────────────────────────────────────────────────────

    private fun inflateBubble() {
        bubbleView = LayoutInflater.from(this).inflate(R.layout.view_bubble, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 300
        }

        var startX = 0; var startY = 0
        var initX = 0; var initY = 0
        var isClick = false

        bubbleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt(); startY = event.rawY.toInt()
                    initX = params.x; initY = params.y
                    isClick = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) isClick = false
                    params.x = initX + dx
                    params.y = initY + dy
                    windowManager.updateViewLayout(bubbleView, params)
                }
                MotionEvent.ACTION_UP -> if (isClick) onBubbleTap()
            }
            true
        }

        windowManager.addView(bubbleView, params)
    }

    private fun onBubbleTap() {
        // Manda broadcast a ScreenCaptureService per catturare
        val intent = Intent(ACTION_CAPTURE).setPackage(packageName)
        sendBroadcast(intent)
        showToast("📸 Cattura in corso...")
    }

    // ── Overlay traduzione ────────────────────────────────────────────────────

    private fun inflateOverlay() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.view_translation_overlay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 100
        }

        overlayView.visibility = View.GONE
        windowManager.addView(overlayView, params)

        overlayView.findViewById<View>(R.id.btnClose)?.setOnClickListener {
            hideOverlay()
        }
    }

    fun showTranslation(original: String, translated: String) {
        overlayView.visibility = View.VISIBLE
        overlayVisible = true
        overlayView.findViewById<TextView>(R.id.tvOriginal)?.text = original
        overlayView.findViewById<TextView>(R.id.tvTranslated)?.text = translated
        // Auto-hide dopo 8 secondi
        scope.launch {
            delay(8000)
            hideOverlay()
        }
    }

    private fun hideOverlay() {
        overlayView.visibility = View.GONE
        overlayVisible = false
    }

    // ── BroadcastReceiver per risultati OCR/traduzione ────────────────────────

    private val translationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ScreenCaptureService.BROADCAST_TRANSLATION) return
            val original = intent.getStringExtra("original") ?: return
            val translated = intent.getStringExtra("translated") ?: return
            showTranslation(original, translated)
        }
    }

    private fun registerTranslationReceiver() {
        val filter = IntentFilter(ScreenCaptureService.BROADCAST_TRANSLATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(translationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(translationReceiver, filter)
        }
        translationReceiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        runCatching { windowManager.removeView(bubbleView) }
        runCatching { windowManager.removeView(overlayView) }
        if (translationReceiverRegistered) {
            runCatching { unregisterReceiver(translationReceiver) }
            translationReceiverRegistered = false
        }
        super.onDestroy()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "LiveTranslateX Bubble",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, FloatingBubbleService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LiveTranslateX attivo")
            .setContentText("Tocca la bolla per tradurre lo schermo")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .build()
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
