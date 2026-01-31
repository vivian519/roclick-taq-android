package com.iceflow.roclicktaq.overlay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.run.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class OverlayService extends Service {
    private WindowManager wm;
    private LinearLayout root;
    private ImageView btnRun;
    private ImageView btnStop;
    private ImageView btnAimBlack;
    private ImageView btnAimRed;
    private ImageView btnAimBlue;
    private ImageView btnClose;
    private BroadcastReceiver stateReceiver;
    private int lastX;
    private int lastY;
    private boolean dragging;
    private long downTs;
    private float startX;
    private float startY;
    private int baseX;
    private int baseY;

    public static void tryStart(Context ctx) {
        if (Settings.canDrawOverlays(ctx)) {
            Intent i = new Intent(ctx, OverlayService.class);
            ctx.startService(i);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(android.net.Uri.parse("package:" + ctx.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    public IBinder onBind(Intent intent) { return null; }

    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(6);
        root.setPadding(pad, pad, pad, pad);
        root.setBackground(makeBg());

        btnRun = new ImageView(this);
        btnRun.setImageResource(R.drawable.ic_play_triangle);
        btnRun.setClickable(true);

        btnStop = new ImageView(this);
        btnStop.setImageResource(R.drawable.ic_stop_square);
        btnStop.setClickable(true);

        btnAimBlack = new ImageView(this);
        btnAimBlack.setImageResource(R.drawable.ic_crosshair_black);
        btnAimBlack.setClickable(true);

        btnAimRed = new ImageView(this);
        btnAimRed.setImageResource(R.drawable.ic_crosshair_red);
        btnAimRed.setClickable(true);
        btnAimBlue = new ImageView(this);
        btnAimBlue.setImageResource(R.drawable.ic_crosshair_blue);
        btnAimBlue.setClickable(true);

        TextView btnTest = new TextView(this);
        btnTest.setText("T");
        btnTest.setTextSize(16);
        btnTest.setTextColor(Color.WHITE);
        btnTest.setClickable(true);
        btnTest.setPadding(dp(2), dp(2), dp(2), dp(2));
        btnTest.setGravity(Gravity.CENTER);

        btnClose = new ImageView(this);
        btnClose.setImageResource(R.drawable.ic_close);
        btnClose.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(20), dp(20));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(btnRun, lp);
        root.addView(btnStop, lp);
        root.addView(btnAimBlack, lp);
        root.addView(btnAimRed, lp);
        root.addView(btnAimBlue, lp);
        root.addView(btnTest, lp);
        root.addView(btnClose, lp);

        btnRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String st = "idle";
                try {
                    SharedPreferences sp = getSharedPreferences("roclick", MODE_PRIVATE);
                    st = sp.getString("runner_state", "idle");
                } catch (Exception ignored) {}
                if (!"running".equals(st)) {
                    try { root.setVisibility(View.INVISIBLE); } catch (Exception ignored) {}
                    Intent it = new Intent(getApplicationContext(), com.iceflow.roclicktaq.ui.PermissionStarterActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(it);
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    applyState("stopped");
                    SharedPreferences sp = getSharedPreferences("roclick", MODE_PRIVATE);
                    sp.edit().putString("runner_state", "stopped").apply();
                    Intent i = new Intent("com.iceflow.roclicktaq.ACTION_RUNNER_STATE");
                    i.putExtra("state", "stopped");
                    sendBroadcast(i);
                } catch (Exception ignored) {}
                try { com.iceflow.roclicktaq.run.Controller.stopScript(getApplicationContext()); } catch (Exception ignored) {}
            }
        });
        btnAimBlack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(getApplicationContext(), com.iceflow.roclicktaq.overlay.AimOverlayServiceBlack.class);
                    getApplicationContext().startService(i);
                } catch (Exception ignored) {}
            }
        });
        btnAimRed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(getApplicationContext(), com.iceflow.roclicktaq.overlay.AimOverlayServiceRed.class);
                    getApplicationContext().startService(i);
                } catch (Exception ignored) {}
            }
        });
        btnAimBlue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(getApplicationContext(), com.iceflow.roclicktaq.overlay.AimOverlayServiceBlue.class);
                    getApplicationContext().startService(i);
                } catch (Exception ignored) {}
            }
        });
        btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    writeFlag("/sdcard/TouchSprite/config/debug_force_black_once.flag");
                } catch (Exception ignored) {}
                try {
                    writeFlag("/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/debug_force_black_once.flag");
                } catch (Exception ignored) {}
                try { Toast.makeText(OverlayService.this, "已触发测试黑点序列", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try { stopSelf(); } catch (Exception ignored) {}
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params.x = dp(8);
        params.y = 0;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.format = android.graphics.PixelFormat.TRANSLUCENT;
        wm.addView(root, params);
        lastX = params.x;
        lastY = params.y;
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, android.view.MotionEvent e) {
                if (e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    downTs = System.currentTimeMillis();
                    startX = e.getRawX();
                    startY = e.getRawY();
                    baseX = lastX;
                    baseY = lastY;
                    dragging = false;
                    return false;
                } else if (e.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                    if (!dragging && System.currentTimeMillis() - downTs >= 350) {
                        dragging = true;
                    }
                    if (dragging) {
                        int nx = baseX + (int) (e.getRawX() - startX);
                        int ny = baseY + (int) (e.getRawY() - startY);
                        WindowManager.LayoutParams p = (WindowManager.LayoutParams) root.getLayoutParams();
                        p.x = nx;
                        p.y = ny;
                        wm.updateViewLayout(root, p);
                        lastX = nx;
                        lastY = ny;
                        return true;
                    }
                    return false;
                } else if (e.getAction() == android.view.MotionEvent.ACTION_UP || e.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    dragging = false;
                    return false;
                }
                return false;
            }
        });
        try {
            SharedPreferences sp = getSharedPreferences("roclick", MODE_PRIVATE);
            String st = sp.getString("runner_state", "idle");
            applyState(st);
        } catch (Exception ignored) {}
        stateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "com.iceflow.roclicktaq.ACTION_RUNNER_STATE".equals(intent.getAction())) {
                    String st = intent.getStringExtra("state");
                    applyState(st);
                }
            }
        };
        try {
            IntentFilter f = new IntentFilter("com.iceflow.roclicktaq.ACTION_RUNNER_STATE");
            registerReceiver(stateReceiver, f);
        } catch (Exception ignored) {}
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "rebuild".equals(intent.getAction())) {
            try {
                if (root != null && root.getParent() == null) {
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                    params.x = dp(8);
                    params.y = 0;
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        params.type = WindowManager.LayoutParams.TYPE_PHONE;
                    }
                    params.format = android.graphics.PixelFormat.TRANSLUCENT;
                    wm.addView(root, params);
                } else if (root != null) {
                    root.setVisibility(View.VISIBLE);
                }
                try {
                    SharedPreferences sp = getSharedPreferences("roclick", MODE_PRIVATE);
                    String st = sp.getString("runner_state", "idle");
                    applyState(st);
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (wm != null && root != null) wm.removeView(root);
        } catch (Exception ignored) {}
        try { unregisterReceiver(stateReceiver); } catch (Exception ignored) {}
    }

    private GradientDrawable makeBg() {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(0x66000000);
        d.setCornerRadius(dp(12));
        d.setStroke(dp(1), Color.WHITE);
        return d;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void applyState(String st) {
        try {
            if ("running".equals(st)) {
                btnRun.setEnabled(false);
                btnRun.setAlpha(0.5f);
                btnRun.setImageResource(R.drawable.ic_play_triangle);
            } else {
                btnRun.setEnabled(true);
                btnRun.setAlpha(1f);
                btnRun.setImageResource(R.drawable.ic_play_triangle);
            }
        } catch (Exception ignored) {}
    }

    private void writeFlag(String path) throws Exception {
        File f = new File(path);
        File d = f.getParentFile();
        if (d != null && !d.exists()) d.mkdirs();
        FileOutputStream fos = new FileOutputStream(f, false);
        fos.write("1".getBytes(StandardCharsets.UTF_8));
        fos.flush();
        fos.close();
    }
}
