package com.iceflow.roclicktaq.overlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.io.ConfigIO;

import org.json.JSONObject;

public class AimOverlayServiceBlack extends Service {
    private WindowManager wm;
    private FrameLayout root;
    private ImageView aim;
    private LinearLayout bar;
    private int lastX;
    private int lastY;

    public IBinder onBind(Intent intent) { return null; }

    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        root = new FrameLayout(this);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.MATCH_PARENT;
        p.gravity = Gravity.START | Gravity.TOP;
        p.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            p.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            p.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        p.format = PixelFormat.TRANSLUCENT;
        wm.addView(root, p);

        aim = new ImageView(this);
        aim.setImageResource(R.drawable.ic_crosshair_black);
        FrameLayout.LayoutParams ap = new FrameLayout.LayoutParams(dp(20), dp(20));
        ap.gravity = Gravity.TOP | Gravity.START;
        ap.leftMargin = dp(80);
        ap.topMargin = dp(80);
        root.addView(aim, ap);

        bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        bp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        bp.bottomMargin = dp(24);
        TextView btnSave = new TextView(this);
        btnSave.setText("保存");
        btnSave.setTextSize(14);
        btnSave.setPadding(dp(12), dp(8), dp(12), dp(8));
        TextView btnClose = new TextView(this);
        btnClose.setText("关闭");
        btnClose.setTextSize(14);
        btnClose.setPadding(dp(12), dp(8), dp(12), dp(8));
        bar.addView(btnSave);
        bar.addView(btnClose);
        root.addView(bar, bp);

        aim.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY;
            int baseL;
            int baseT;
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = e.getRawX();
                    startY = e.getRawY();
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) aim.getLayoutParams();
                    baseL = lp.leftMargin;
                    baseT = lp.topMargin;
                    return true;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    float dx = e.getRawX() - startX;
                    float dy = e.getRawY() - startY;
                    int nl = Math.max(0, baseL + (int) dx);
                    int nt = Math.max(0, baseT + (int) dy);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) aim.getLayoutParams();
                    lp.leftMargin = nl;
                    lp.topMargin = nt;
                    lastX = nl;
                    lastY = nt;
                    aim.setLayoutParams(lp);
                    return true;
                }
                return false;
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopSelf();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) aim.getLayoutParams();
                    int ax = lp.leftMargin + dp(10);
                    int ay = lp.topMargin + dp(10);
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    int sw = dm.widthPixels;
                    int sh = dm.heightPixels;
                    JSONObject cfg = ConfigIO.readConfig();
                    int baseW = cfg.optInt("baseW", 1080);
                    int baseH = cfg.optInt("baseH", 2280);
                    int fx = Math.round((float) ax * baseW / sw);
                    int fy = Math.round((float) ay * baseH / sh);
                    cfg.put("fixed_black_cx", fx);
                    cfg.put("fixed_black_cy", fy);
                    boolean ok = ConfigIO.writeConfig(cfg);
                    Toast.makeText(AimOverlayServiceBlack.this, ok ? "坐标已保存" : "保存失败", Toast.LENGTH_SHORT).show();
                    stopSelf();
                } catch (Exception e) {
                    Toast.makeText(AimOverlayServiceBlack.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        try { if (wm != null && root != null) wm.removeView(root); } catch (Exception ignored) {}
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }
}
