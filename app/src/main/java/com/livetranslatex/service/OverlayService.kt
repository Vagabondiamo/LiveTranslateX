package com.livetranslatex.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.livetranslatex.domain.model.TranslationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val overlayViews = mutableListOf<TextView>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    fun showTranslations(results: List<TranslationResult>) {
        clearOverlays()
        results.forEach { result ->
            val textView = TextView(this).apply {
                text = result.translated
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.argb(180, 0, 0, 0))
                textSize = 14f
                setPadding(8, 4, 8, 4)
            }

            val params = WindowManager.LayoutParams(
                result.bounds.width(),
                WindowManager.LayoutParams.WRAP_CONTENT,
                result.bounds.left,
                result.bounds.top,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager.addView(textView, params)
            overlayViews.add(textView)
        }
    }

    fun clearOverlays() {
        overlayViews.forEach {
            runCatching { windowManager.removeView(it) }
        }
        overlayViews.clear()
    }

    override fun onDestroy() {
        clearOverlays()
        super.onDestroy()
    }
}
