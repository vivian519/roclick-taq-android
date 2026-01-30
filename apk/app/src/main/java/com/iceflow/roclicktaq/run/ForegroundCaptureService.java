package com.iceflow.roclicktaq.run;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.content.SharedPreferences;
import android.content.IntentFilter;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.io.LogIO;
import com.iceflow.roclicktaq.ui.MainActivity;

public class ForegroundCaptureService extends Service {
    private static final String CHANNEL_ID = "roclick_fg";
    private CaptureManager cap;
    private Runner runner;

    public IBinder onBind(Intent intent) { return null; }

    public void onCreate() {
        super.onCreate();
        try { LogIO.write("error.log", "FGS onCreate"); } catch (Exception ignored) {}
        createChannel();
        try {
            Notification n = buildNotification("独立运行准备中");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } else {
                startForeground(1, n);
            }
        } catch (Exception e) {
            try { LogIO.write("error.log", "startForeground异常"); } catch (Exception ignored) {}
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if ("stop".equals(action)) {
            try { if (runner != null) runner.stop(); } catch (Exception ignored) {}
            try { if (cap != null) cap.stop(); } catch (Exception ignored) {}
            try { stopForeground(true); } catch (Exception ignored) {}
            stopSelf();
            updateState("stopped");
            return START_NOT_STICKY;
        } else if ("stop_script".equals(action)) {
            try { if (runner != null) runner.stop(); } catch (Exception ignored) {}
            try {
                Notification n = buildNotification("脚本已停止");
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(1, n);
            } catch (Exception ignored) {}
            updateState("stopped");
            return START_STICKY;
        } else if ("pause".equals(action)) {
            try { if (runner != null) runner.pause(); } catch (Exception ignored) {}
            try {
                Notification n = buildNotification("已暂停");
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(1, n);
            } catch (Exception ignored) {}
            updateState("paused");
            return START_STICKY;
        } else if ("resume".equals(action)) {
            try { if (runner != null) runner.resume(); } catch (Exception ignored) {}
            try {
                Notification n = buildNotification("独立运行中");
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(1, n);
            } catch (Exception ignored) {}
            updateState("running");
            return START_STICKY;
        }
        int rc = intent != null ? intent.getIntExtra("resultCode", 0) : 0;
        Intent data = intent != null ? intent.getParcelableExtra("data") : null;
        try { LogIO.write("error.log", "前台服务收到授权 rc=" + rc + " data=" + (data != null)); } catch (Exception ignored) {}
        new Thread(() -> {
            try {
                if (cap == null) cap = new CaptureManager();
                if (runner == null) runner = new Runner();
                boolean ok = cap.start(this, rc, data);
                if (ok) {
                    try {
                        Notification n = buildNotification("独立运行中");
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(1, n);
                    } catch (Exception e) {
                        try { LogIO.write("error.log", "notify异常"); } catch (Exception ignored) {}
                    }
                    runner.start(cap);
                    updateState("running");
                } else {
                    try { LogIO.write("error.log", "服务启动失败"); } catch (Exception ignored) {}
                    stopSelf();
                }
            } catch (Exception e) {
                try { LogIO.write("error.log", "服务异常"); } catch (Exception ignored) {}
                stopSelf();
            }
        }).start();
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        try { if (runner != null) runner.stop(); } catch (Exception ignored) {}
        try { if (cap != null) cap.stop(); } catch (Exception ignored) {}
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "ROClick 前台服务", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder b = new Notification.Builder(this)
                .setContentTitle("ROClickTAQ")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_play_triangle)
                .setContentIntent(pi)
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b.setChannelId(CHANNEL_ID);
        }
        return b.build();
    }

    private void updateState(String s) {
        try {
            SharedPreferences sp = getSharedPreferences("roclick", MODE_PRIVATE);
            sp.edit().putString("runner_state", s).apply();
        } catch (Exception ignored) {}
        try {
            Intent i = new Intent("com.iceflow.roclicktaq.ACTION_RUNNER_STATE");
            i.putExtra("state", s);
            sendBroadcast(i);
        } catch (Exception ignored) {}
    }
}
