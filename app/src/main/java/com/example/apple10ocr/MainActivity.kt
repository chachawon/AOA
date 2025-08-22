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

    // [추가] 알림 권한 요청을 위한 런처
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한이 허용되면 화면 캡처 요청
            requestCapture()
        } else {
            Toast.makeText(this, "알림 권한이 거부되었습니다. 포그라운드 서비스가 정상 동작하지 않을 수 있습니다.", Toast.LENGTH_LONG).show()
            // 권한이 거부되어도 일단 진행은 하되, 사용자에게 알림
            requestCapture()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            ensureOverlay {
                ensureNotifications {
                    // [수정] 이 콜백은 이제 ensureNotifications 내부 로직으로 처리됩니다.
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

    // [수정] 알림 권한 요청 로직 전체 수정
    private fun ensureNotifications(next: () -> Unit) {
        // Android 13 (API 33) 이상에서만 알림 권한 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한이 이미 있는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                next() // 권한이 있으면 바로 다음 단계 진행
            } else {
                // 권한이 없으면 런처를 통해 요청 (콜백 next()는 여기서 직접 호출하지 않음)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            next() // Android 13 미만에서는 권한이 필요 없음
        }
    }

    private fun requestCapture() {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val intent = mgr.createScreenCaptureIntent()
        captureLauncher.launch(intent)
    }
}
