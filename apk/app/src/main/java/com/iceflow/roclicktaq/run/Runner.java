package com.iceflow.roclicktaq.run;

import android.graphics.Bitmap;

import com.iceflow.roclicktaq.io.ConfigIO;
import com.iceflow.roclicktaq.io.LogIO;

import org.json.JSONObject;

public class Runner {
    private Thread t;
    private volatile boolean running;
    private volatile boolean paused;
    private CaptureManager cap;
    private JSONObject cfg;
    private int lastWindowKey = -1;
    private int lastBlackWindowKey = -1;
    private boolean debugForcedDone = false;
    public void start(CaptureManager cm) {
        this.cap = cm;
        this.cfg = ConfigIO.readConfig();
        if (t != null && t.isAlive()) return;
        running = true;
        paused = false;
        t = new Thread(() -> loop());
        t.start();
    }
    public void stop() {
        running = false;
        try { if (t != null) t.join(800); } catch (Exception e) {}
        t = null;
    }
    public void pause() {
        paused = true;
    }
    public void resume() {
        if (t != null && t.isAlive()) {
            paused = false;
        } else {
            if (cap != null) start(cap);
        }
    }
    private int sx(int x) {
        int w = cap.getW();
        int baseW = cfg.optInt("baseW", 1080);
        return (int)Math.floor((double)x * w / baseW);
    }
    private int sy(int y) {
        int h = cap.getH();
        int baseH = cfg.optInt("baseH", 2280);
        return (int)Math.floor((double)y * h / baseH);
    }
    private Rect attackRect() {
        int w = cap.getW();
        int h = cap.getH();
        int rl = cfg.optInt("rect_left", 260);
        int rr = cfg.optInt("rect_right", 42);
        int rt = cfg.optInt("rect_top", 270);
        int rb = cfg.optInt("rect_bottom", 40);
        int xa = w - sx(rl);
        int xb = w - sx(rr);
        int ya = h - sy(rt);
        int yb = h - sy(rb);
        int x1 = Math.min(xa, xb);
        int x2 = Math.max(xa, xb);
        int y1 = Math.min(ya, yb);
        int y2 = Math.max(ya, yb);
        return new Rect(x1, y1, x2, y2);
    }
    private static class Rect {
        int x1,y1,x2,y2;
        Rect(int a,int b,int c,int d){x1=a;y1=b;x2=c;y2=d;}
        int cx(){ return (x1+x2)/2; }
        int cy(){ return (y1+y2)/2; }
        int w(){ return Math.max(0,x2-x1); }
        int h(){ return Math.max(0,y2-y1); }
    }
    private boolean isBlue(int color) {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        return b > 96 && b > r + 18 && b > g + 18;
    }
    private double blueRatio(Bitmap bmp, Rect r, int step) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int x1 = Math.max(0, Math.min(w-1, r.x1));
        int y1 = Math.max(0, Math.min(h-1, r.y1));
        int x2 = Math.max(0, Math.min(w-1, r.x2));
        int y2 = Math.max(0, Math.min(h-1, r.y2));
        int cnt = 0;
        int blue = 0;
        for (int y = y1; y < y2; y += Math.max(1, step)) {
            for (int x = x1; x < x2; x += Math.max(1, step)) {
                int c = bmp.getPixel(x, y);
                if (isBlue(c)) blue++;
                cnt++;
            }
        }
        if (cnt == 0) return 0;
        return (double)blue / (double)cnt;
    }
    private boolean timeAllowed() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int m = c.get(java.util.Calendar.MINUTE);
        int h = c.get(java.util.Calendar.HOUR_OF_DAY);
        return m == 5 && (h % 2) == 0;
    }
    private int windowKey() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int d = c.get(java.util.Calendar.DAY_OF_YEAR);
        int h = c.get(java.util.Calendar.HOUR_OF_DAY);
        return d * 100 + h;
    }
    private void loop() {
        int w = cap.getW();
        int h = cap.getH();
        String logName = "cancel_attack.log";
        LogIO.write(logName, "启动 分辨率 " + w + "x" + h);
        while (running) {
            try {
                try { com.iceflow.roclicktaq.io.LogCleaner.maybeCleanNow(); } catch (Exception ignored) {}
                if (paused) {
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                }
                boolean debugFileA = new java.io.File("/sdcard/TouchSprite/config/debug_force_black_once.flag").exists();
                boolean debugFileB = new java.io.File("/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/debug_force_black_once.flag").exists();
                boolean flagFiles = debugFileA || debugFileB;
                boolean cfgFlag = cfg.optBoolean("debug_force_black_once", false);
                boolean shouldDebug = flagFiles || (!debugForcedDone && cfgFlag);
                if (shouldDebug) {
                    boolean startedSeq = false;
                    int fcx = cfg.optInt("fixed_red_cx", cfg.optInt("fixed_cx", cfg.optInt("baseW", 1080) / 2));
                    int fcy = cfg.optInt("fixed_red_cy", cfg.optInt("fixed_cy", cfg.optInt("baseH", 2280) / 2));
                    int rx = sx(fcx);
                    int ry = sy(fcy);
                    Bitmap rbmp = null;
                    for (int i = 0; i < 10; i++) {
                        rbmp = cap.capture();
                        if (rbmp != null) break;
                        sleep(200);
                    }
                    if (rbmp != null) {
                        int step = cfg.optInt("sample_step", 6);
                        int rad = Math.max(6, Math.min(w, h) / 64);
                        Rect rr = new Rect(Math.max(0, rx - rad), Math.max(0, ry - rad), Math.min(w - 1, rx + rad), Math.min(h - 1, ry + rad));
                        double rratio = blueRatio(rbmp, rr, step);
                        LogIO.write(logName, "调试 红色占比 " + String.format(java.util.Locale.US,"%.3f",rratio));
                        boolean redBlue = rratio >= cfg.optDouble("blue_ratio", 0.6);
                        if (!redBlue) {
                            org.json.JSONArray arr = cfg.optJSONArray("fixed_black_points");
                            if (arr == null || arr.length() == 0) {
                                int fbx = cfg.optInt("fixed_black_cx", 0);
                                int fby = cfg.optInt("fixed_black_cy", 0);
                                if (fbx > 0 || fby > 0) {
                                    arr = new org.json.JSONArray();
                                    org.json.JSONObject pt = new org.json.JSONObject();
                                    pt.put("x", fbx);
                                    pt.put("y", fby);
                                    arr.put(pt);
                                }
                            }
                            if (arr != null && arr.length() > 0) {
                                LogIO.write(logName, "调试 开始黑点点击序列");
                                startedSeq = true;
                                for (int i = 0; i < arr.length(); i++) {
                                    org.json.JSONObject pt = arr.optJSONObject(i);
                                    if (pt == null) continue;
                                    int bx = sx(pt.optInt("x", 0));
                                    int by = sy(pt.optInt("y", 0));
                                    if (ActionService.available()) {
                                        boolean ok = ActionService.click(bx, by);
                                        LogIO.write(logName, ok ? ("调试 黑点"+i+" 点击") : "点击失败");
                                    } else {
                                        LogIO.write(logName, "未启用辅助服务");
                                    }
                                    sleep(2000);
                                    Bitmap cbmp = null;
                                    for (int k = 0; k < 6; k++) {
                                        cbmp = cap.capture();
                                        if (cbmp != null) break;
                                        sleep(150);
                                    }
                                    if (cbmp != null) {
                                        double cr = blueRatio(cbmp, rr, step);
                                        LogIO.write(logName, "调试 红色占比 " + String.format(java.util.Locale.US,"%.3f",cr));
                                        if (cr >= cfg.optDouble("blue_ratio", 0.6)) {
                                            LogIO.write(logName, "调试 红色已变蓝，停止黑点点击");
                                            break;
                                        }
                                    } else {
                                        LogIO.write("error.log", "捕获失败");
                                    }
                                }
                            } else {
                                LogIO.write(logName, "黑点坐标为空");
                            }
                        }
                    } else {
                        LogIO.write("error.log", "捕获失败");
                    }
                    if (startedSeq) {
                        try { new java.io.File("/sdcard/TouchSprite/config/debug_force_black_once.flag").delete(); } catch (Exception ignored) {}
                        try { new java.io.File("/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/debug_force_black_once.flag").delete(); } catch (Exception ignored) {}
                        if (cfgFlag) {
                            debugForcedDone = true;
                            try {
                                cfg.put("debug_force_black_once", false);
                                com.iceflow.roclicktaq.io.ConfigIO.writeConfig(cfg);
                            } catch (Exception ignored) {}
                        }
                    }
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                }
                {
                    java.util.Calendar sc = java.util.Calendar.getInstance();
                    int sm = sc.get(java.util.Calendar.MINUTE);
                    int sh = sc.get(java.util.Calendar.HOUR_OF_DAY);
                    int ss = sc.get(java.util.Calendar.SECOND);
                    boolean secAllowed = sm == 0 && (sh % 2) == 0 && ss >= 4 && ss <= 6;
                    if (secAllowed) {
                        int bwk = windowKey();
                        if (bwk != lastBlackWindowKey) {
                            lastBlackWindowKey = bwk;
                            LogIO.write(logName, "进入05秒黑点窗口");
                            int fcx = cfg.optInt("fixed_red_cx", cfg.optInt("fixed_cx", cfg.optInt("baseW", 1080) / 2));
                            int fcy = cfg.optInt("fixed_red_cy", cfg.optInt("fixed_cy", cfg.optInt("baseH", 2280) / 2));
                            int rx = sx(fcx);
                            int ry = sy(fcy);
                            Bitmap rbmp = cap.capture();
                            if (rbmp != null) {
                                int step = cfg.optInt("sample_step", 6);
                                int rad = Math.max(6, Math.min(w, h) / 64);
                                Rect rr = new Rect(Math.max(0, rx - rad), Math.max(0, ry - rad), Math.min(w - 1, rx + rad), Math.min(h - 1, ry + rad));
                                double rratio = blueRatio(rbmp, rr, step);
                                LogIO.write(logName, "红色占比 " + String.format(java.util.Locale.US,"%.3f",rratio));
                                boolean redBlue = rratio >= cfg.optDouble("blue_ratio", 0.6);
                                if (!redBlue) {
                                    org.json.JSONArray arr = cfg.optJSONArray("fixed_black_points");
                                    if (arr == null || arr.length() == 0) {
                                        int fbx = cfg.optInt("fixed_black_cx", 0);
                                        int fby = cfg.optInt("fixed_black_cy", 0);
                                        if (fbx > 0 || fby > 0) {
                                            arr = new org.json.JSONArray();
                                            org.json.JSONObject pt = new org.json.JSONObject();
                                            pt.put("x", fbx);
                                            pt.put("y", fby);
                                            arr.put(pt);
                                        }
                                    }
                                    if (arr != null && arr.length() > 0) {
                                        LogIO.write(logName, "开始黑点点击序列");
                                        for (int i = 0; i < arr.length(); i++) {
                                            org.json.JSONObject pt = arr.optJSONObject(i);
                                            if (pt == null) continue;
                                            int bx = sx(pt.optInt("x", 0));
                                            int by = sy(pt.optInt("y", 0));
                                            if (ActionService.available()) {
                                                boolean ok = ActionService.click(bx, by);
                                                LogIO.write(logName, ok ? ("黑点"+i+" 点击") : "点击失败");
                                            } else {
                                                LogIO.write(logName, "未启用辅助服务");
                                            }
                                            sleep(2000);
                                            Bitmap cbmp = cap.capture();
                                            if (cbmp != null) {
                                                double cr = blueRatio(cbmp, rr, step);
                                                LogIO.write(logName, "红色占比 " + String.format(java.util.Locale.US,"%.3f",cr));
                                                if (cr >= cfg.optDouble("blue_ratio", 0.6)) {
                                                    LogIO.write(logName, "红色已变蓝，停止黑点点击");
                                                    break;
                                                }
                                            } else {
                                                LogIO.write("error.log", "捕获失败");
                                            }
                                        }
                                    } else {
                                        LogIO.write(logName, "黑点坐标为空");
                                    }
                                }
                            } else {
                                LogIO.write("error.log", "捕获失败");
                            }
                            sleep(cfg.optInt("interval_ms", 500));
                            continue;
                        }
                    }
                }
                if (!timeAllowed()) {
                    LogIO.write(logName, "时间未到");
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                }
                int wk = windowKey();
                if (wk == lastWindowKey) {
                    LogIO.write(logName, "本时段已执行");
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                } else {
                    lastWindowKey = wk;
                    LogIO.write(logName, "进入目标时间窗口");
                }
                String mode = cfg.optString("mode", "image");
                if ("fixed_red".equalsIgnoreCase(mode) || "fixed".equalsIgnoreCase(mode)) {
                    int fcx = cfg.optInt("fixed_red_cx", cfg.optInt("fixed_cx", cfg.optInt("baseW", 1080) / 2));
                    int fcy = cfg.optInt("fixed_red_cy", cfg.optInt("fixed_cy", cfg.optInt("baseH", 2280) / 2));
                    int cx = sx(fcx);
                    int cy = sy(fcy);
                    Bitmap fbmp = cap.capture();
                    if (fbmp != null) {
                        int step = cfg.optInt("sample_step", 6);
                        int rad = Math.max(6, Math.min(w, h) / 64);
                        Rect fr = new Rect(Math.max(0, cx - rad), Math.max(0, cy - rad), Math.min(w - 1, cx + rad), Math.min(h - 1, cy + rad));
                        double ratio = blueRatio(fbmp, fr, step);
                        LogIO.write(logName, "固定点蓝色占比 " + String.format(java.util.Locale.US,"%.3f",ratio));
                        boolean shouldClick = ratio >= cfg.optDouble("blue_ratio", 0.6);
                        if (shouldClick && ActionService.available()) {
                            boolean ok = ActionService.click(cx, cy);
                            LogIO.write(logName, ok ? "固定坐标点击" : "点击失败");
                            sleep(cfg.optInt("confirm_ms", 800));
                        } else if (shouldClick) {
                            LogIO.write(logName, "未启用辅助服务");
                        }
                    } else {
                        LogIO.write("error.log", "捕获失败");
                    }
                    int bfx = cfg.optInt("fixed_blue_cx", 0);
                    int bfy = cfg.optInt("fixed_blue_cy", 0);
                    if (bfx > 0 || bfy > 0) {
                        int bx = sx(bfx);
                        int by = sy(bfy);
                        if (ActionService.available()) {
                            boolean bok = ActionService.click(bx, by);
                            LogIO.write(logName, bok ? "蓝色坐标点击" : "点击失败");
                            sleep(cfg.optInt("confirm_ms", 800));
                        } else {
                            LogIO.write(logName, "未启用辅助服务");
                        }
                    }
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                } else if ("fixed_black".equalsIgnoreCase(mode)) {
                    LogIO.write(logName, "黑色准星模式未配置逻辑");
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                }
                Bitmap bmp = cap.capture();
                if (bmp == null) {
                    LogIO.write("error.log", "捕获失败");
                    sleep(cfg.optInt("interval_ms", 500));
                    continue;
                }
                Rect r = attackRect();
                boolean needCancel;
                if ("color".equalsIgnoreCase(mode)) {
                    int step = cfg.optInt("sample_step", 6);
                    double ratio = blueRatio(bmp, r, step);
                    LogIO.write(logName, "蓝色占比 " + String.format(java.util.Locale.US,"%.3f",ratio));
                    needCancel = ratio >= cfg.optDouble("blue_ratio", 0.6);
                } else {
                    int step = cfg.optInt("sample_step", 6);
                    double ratio = blueRatio(bmp, r, step);
                    LogIO.write(logName, "蓝色占比 " + String.format(java.util.Locale.US,"%.3f",ratio));
                    needCancel = ratio >= cfg.optDouble("blue_ratio", 0.6);
                }
                if (needCancel && ActionService.available()) {
                    int cx = r.cx();
                    int cy = r.cy();
                    boolean ok = ActionService.click(cx, cy);
                    LogIO.write(logName, ok ? "点击取消" : "点击失败");
                    sleep(cfg.optInt("confirm_ms", 800));
                } else if (needCancel) {
                    LogIO.write(logName, "未启用辅助服务");
                }
                sleep(cfg.optInt("interval_ms", 500));
            } catch (Exception e) {
                LogIO.write("error.log", "循环异常");
                sleep(cfg.optInt("interval_ms", 500));
            }
        }
        LogIO.write(logName, "结束");
    }
    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}
