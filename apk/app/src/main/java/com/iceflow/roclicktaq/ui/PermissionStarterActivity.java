package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.widget.Toast;
import android.view.Gravity;

public class PermissionStarterActivity extends Activity {
    private static final int REQ_NOTIF = 10086;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void onResume() {
        super.onResume();
        try {
            Toast t = Toast.makeText(this, "即将开始脚本，授权完成后自动运行", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.BOTTOM, 0, 80);
            t.show();
        } catch (Exception ignored) {}
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                return;
            }
        }
        Intent it = new Intent(this, com.iceflow.roclicktaq.ui.CapturePermissionActivity.class);
        startActivity(it);
        finish();
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            try {
                Toast t = Toast.makeText(this, "即将开始脚本，授权完成后自动运行", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.BOTTOM, 0, 80);
                t.show();
            } catch (Exception ignored) {}
            Intent it = new Intent(this, com.iceflow.roclicktaq.ui.CapturePermissionActivity.class);
            startActivity(it);
            finish();
        }
    }
}
