package com.iceflow.roclicktaq.run;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ActionService extends AccessibilityService {
    private static ActionService s;
    public static boolean available() {
        return s != null;
    }
    public static boolean click(int x, int y) {
        if (s == null) return false;
        Path p = new Path();
        p.moveTo(x, y);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 120);
        GestureDescription gd = new GestureDescription.Builder().addStroke(sd).build();
        final boolean[] ok = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> s.dispatchGesture(gd, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription gestureDescription) { ok[0] = true; latch.countDown(); }
            @Override public void onCancelled(GestureDescription gestureDescription) { ok[0] = false; latch.countDown(); }
        }, null));
        try { latch.await(600, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
        return ok[0];
    }
    @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override protected void onServiceConnected() { s = this; }
    @Override public boolean onUnbind(android.content.Intent intent) { s = null; return super.onUnbind(intent); }
}
