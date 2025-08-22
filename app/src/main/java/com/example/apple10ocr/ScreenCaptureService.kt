
package com.example.apple10ocr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.DisplayManager
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class ScreenCaptureService : Service() {

    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private lateinit var wm: WindowManager
    private var overlay: OverlayView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ocr: OCRProcessor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ocr = OCRProcessor(this)
        startAsForeground()
    }

    private fun startAsForeground() {
        val channelId = "capture_channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(channelId, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Apple10OCR")
            .setContentText(getString(R.string.channel_desc))
            .setContentIntent(pi)
            .build()
        startForeground(1, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RC, -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>(EXTRA_INTENT)
        if (resultCode == -1 || data == null) {
            stopSelf(); return START_NOT_STICKY
        }
        startProjection(resultCode, data)
        showOverlay()
        return START_STICKY
    }

    private fun startProjection(resultCode: Int, data: Intent) {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(resultCode, data)

        val dm = resources.displayMetrics
        reader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 2)

        projection!!.createVirtualDisplay(
            "apple10ocr",
            dm.widthPixels, dm.heightPixels, dm.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface, null, null
        )

        reader!!.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * image.width
                val bmp = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
                bmp.copyPixelsFromBuffer(buffer)
                val cropped = Bitmap.createBitmap(bmp, 0, 0, image.width, image.height)

                scope.launch {
                    val cells = ocr?.process(cropped)
                    withContext(Dispatchers.Main) {
                        overlay?.updateFrame(cropped.width, cropped.height, cells ?: emptyList())
                    }
                }
            } catch (t: Throwable) {
                Log.e("Apple10OCR", "image error", t)
            } finally {
                image.close()
            }
        }, null)
    }

    private fun showOverlay() {
        if (overlay != null) return
        overlay = OverlayView(this)
        val lp = android.view.WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= 26)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(overlay, lp)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { wm.removeViewImmediate(overlay) } catch (_: Throwable) {}
        reader?.close()
        projection?.stop()
        scope.cancel()
    }

    companion object {
        private const val EXTRA_RC = "rc"
        private const val EXTRA_INTENT = "intent"
        fun start(ctx: Context, rc: Int, data: Intent) {
            val i = Intent(ctx, ScreenCaptureService::class.java)
            i.putExtra(EXTRA_RC, rc)
            i.putExtra(EXTRA_INTENT, data)
            ContextCompat.startForegroundService(ctx, i)
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ScreenCaptureService::class.java))
        }
    }
}
