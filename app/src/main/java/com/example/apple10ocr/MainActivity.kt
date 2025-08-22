
package com.example.apple10ocr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK && res.data != null) {
            ScreenCaptureService.start(this, res.resultCode, res.data!!)
        } else {
            Toast.makeText(this, "화면 캡처가 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            ensureOverlay {
                ensureNotifications {
                    requestCapture()
                }
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            ScreenCaptureService.stop(this)
        }
    }

    private fun ensureOverlay(next: () -> Unit) {
        if (Settings.canDrawOverlays(this)) {
            next(); return
        }
        val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(i)
        Toast.makeText(this, "오버레이 권한을 켜고 다시 시도하세요.", Toast.LENGTH_LONG).show()
    }

    private fun ensureNotifications(next: () -> Unit) {
        if (Build.VERSION.SDK_INT < 33) { next(); return }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        next()
    }

    private fun requestCapture() {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val intent = mgr.createScreenCaptureIntent()
        captureLauncher.launch(intent)
    }
}
