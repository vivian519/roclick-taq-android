package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import com.iceflow.roclicktaq.run.CaptureManager;
import com.iceflow.roclicktaq.run.Controller;

public class CapturePermissionActivity extends Activity {
    private boolean requested = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void onResume() {
        super.onResume();
        if (!requested) {
            requested = true;
            MediaProjectionManager m = (MediaProjectionManager) getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);
            Intent it = m.createScreenCaptureIntent();
            startActivityForResult(it, CaptureManager.REQ_CODE);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureManager.REQ_CODE) {
            Controller.startWithPermission(this, resultCode, data);
        }
        try { moveTaskToBack(true); } catch (Exception ignored) {}
        finish();
    }
}
