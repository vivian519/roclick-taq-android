package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.io.ConfigIO;
import com.iceflow.roclicktaq.run.CaptureManager;
import com.iceflow.roclicktaq.run.Controller;
import com.iceflow.roclicktaq.run.Runner;
import com.iceflow.roclicktaq.overlay.OverlayService;

public class MainActivity extends Activity {
    private CaptureManager cap;
    private Runner runner;
    private boolean autoCaptureRequested;
    private boolean retryRequested;
    private static final int REQ_NOTIF = 10086;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OverlayService.tryStart(this);
        autoCaptureRequested = false;
        try { com.iceflow.roclicktaq.io.LogIO.write("error.log", "MainActivity onCreate"); } catch (Exception ignored) {}
        Button btnLogs = findViewById(R.id.btn_logs);
        Button btnBlackPoints = findViewById(R.id.btn_black_points_settings);
        Button btnGrant = findViewById(R.id.btn_grant_all_files);
        cap = new CaptureManager();
        runner = new Runner();
        btnGrant.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!ConfigIO.canManageAllFiles(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "已拥有所有文件访问权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnLogs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LogsActivity.class));
            }
        });
        btnBlackPoints.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BlackPointsSettingsActivity.class));
            }
        });
        ;
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("auto_capture", false)) {
            autoCaptureRequested = true;
        }
    }
    protected void onResume() {
        super.onResume();
        try { com.iceflow.roclicktaq.io.LogIO.write("error.log", "MainActivity onResume"); } catch (Exception ignored) {}
        try {
        } catch (Exception ignored) {}
        if (autoCaptureRequested) {
            autoCaptureRequested = false;
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(() -> {
                try {
                    stopService(new Intent(MainActivity.this, OverlayService.class));
                } catch (Exception ignored) {}
                try { com.iceflow.roclicktaq.io.LogIO.write("error.log", "准备请求权限"); } catch (Exception ignored) {}
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                        return;
                    }
                }
                if (!ConfigIO.canManageAllFiles(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    return;
                }
                Intent it = cap.createIntent(MainActivity.this);
                startActivityForResult(it, CaptureManager.REQ_CODE);
            }, 400);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        try { } catch (Exception ignored) {}
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(() -> {
                Intent it = cap.createIntent(MainActivity.this);
                startActivityForResult(it, CaptureManager.REQ_CODE);
            }, 300);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureManager.REQ_CODE) {
            try {
                com.iceflow.roclicktaq.io.LogIO.write("error.log", "授权返回 rc=" + resultCode + " data=" + (data != null));
            } catch (Exception ignored) {}
            if ((resultCode != RESULT_OK || data == null) && !retryRequested) {
                retryRequested = true;
                Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(() -> {
                    Intent it = cap.createIntent(MainActivity.this);
                    startActivityForResult(it, CaptureManager.REQ_CODE);
                }, 800);
                return;
            }
            com.iceflow.roclicktaq.run.Controller.startWithPermission(this, resultCode, data);
            Intent s = new Intent(this, OverlayService.class);
            s.setAction("rebuild");
            startService(s);
            OverlayService.tryStart(this);
            try { } catch (Exception ignored) {}
        }
    }
}
