package com.iceflow.roclicktaq.run;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.NotificationManager;
import android.content.Context;
import com.iceflow.roclicktaq.io.LogIO;
import android.os.Build;

public class Controller {
    private static CaptureManager cap;
    private static Runner runner;

    public static void requestStart(Context ctx) {
        Intent it = new Intent(ctx, com.iceflow.roclicktaq.ui.MainActivity.class);
        it.putExtra("auto_capture", true);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(it);
    }

    public static void startWithPermission(Activity act, int resultCode, Intent data) {
        try {
            if (resultCode != Activity.RESULT_OK || data == null) {
                Intent i = new Intent(act, com.iceflow.roclicktaq.ui.MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("auto_capture", true);
                act.startActivity(i);
                try {
                    Intent os = new Intent(act.getApplicationContext(), com.iceflow.roclicktaq.overlay.OverlayService.class);
                    os.setAction("rebuild");
                    act.getApplicationContext().startService(os);
                } catch (Exception ignored) {}
                return;
            }
        } catch (Exception ignored) {}
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            boolean okDirect = false;
            try {
                if (cap == null) cap = new CaptureManager();
                if (runner == null) runner = new Runner();
                okDirect = cap.start(act, resultCode, data);
                if (okDirect) {
                    runner.start(cap);
                    try {
                        Intent os = new Intent(act.getApplicationContext(), com.iceflow.roclicktaq.overlay.OverlayService.class);
                        os.setAction("rebuild");
                        act.getApplicationContext().startService(os);
                    } catch (Exception ignored) {}
                    return;
                }
            } catch (Exception e) {
                okDirect = false;
            }
        }
        try {
            Intent s = new Intent(act.getApplicationContext(), ForegroundCaptureService.class);
            s.putExtra("resultCode", resultCode);
            s.putExtra("data", data);
            if (Build.VERSION.SDK_INT >= 26) {
                act.getApplicationContext().startForegroundService(s);
            } else {
                act.getApplicationContext().startService(s);
            }
            try {
                Intent os = new Intent(act.getApplicationContext(), com.iceflow.roclicktaq.overlay.OverlayService.class);
                os.setAction("rebuild");
                act.getApplicationContext().startService(os);
            } catch (Exception ignored) {}
            try { act.moveTaskToBack(true); } catch (Exception ignored) {}
        } catch (Exception e) {
            try { LogIO.write("error.log", "前台服务启动异常"); } catch (Exception ignored) {}
        }
    }

    public static void stop() {
        try { if (runner != null) runner.stop(); } catch (Exception ignored) {}
        try { if (cap != null) cap.stop(); } catch (Exception ignored) {}
    }
    public static void stop(Context ctx) {
        try {
            Intent i = new Intent(ctx.getApplicationContext(), ForegroundCaptureService.class);
            i.setAction("stop");
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.getApplicationContext().startForegroundService(i);
            } else {
                ctx.getApplicationContext().startService(i);
            }
        } catch (Exception ignored) {}
    }
    public static void stopScript(Context ctx) {
        try { if (runner != null) runner.stop(); } catch (Exception ignored) {}
        try {
            Intent i = new Intent(ctx.getApplicationContext(), ForegroundCaptureService.class);
            i.setAction("stop_script");
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.getApplicationContext().startForegroundService(i);
            } else {
                ctx.getApplicationContext().startService(i);
            }
        } catch (Exception ignored) {}
    }
    public static void pause(Context ctx) {
        try { if (runner != null) runner.pause(); } catch (Exception ignored) {}
        try {
            Intent i = new Intent(ctx.getApplicationContext(), ForegroundCaptureService.class);
            i.setAction("pause");
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.getApplicationContext().startForegroundService(i);
            } else {
                ctx.getApplicationContext().startService(i);
            }
        } catch (Exception ignored) {}
    }
    public static void resume(Context ctx) {
        try { if (runner != null) runner.resume(); } catch (Exception ignored) {}
        try {
            Intent i = new Intent(ctx.getApplicationContext(), ForegroundCaptureService.class);
            i.setAction("resume");
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.getApplicationContext().startForegroundService(i);
            } else {
                ctx.getApplicationContext().startService(i);
            }
        } catch (Exception ignored) {}
    }
}
