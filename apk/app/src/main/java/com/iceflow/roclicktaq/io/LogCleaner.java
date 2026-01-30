package com.iceflow.roclicktaq.io;

import java.io.File;
import java.io.FileOutputStream;

public class LogCleaner {
    private static int lastCleanDay = -1;
    public static void maybeCleanNow() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int h = c.get(java.util.Calendar.HOUR_OF_DAY);
        int m = c.get(java.util.Calendar.MINUTE);
        int d = c.get(java.util.Calendar.DAY_OF_YEAR);
        if (h == 8 && m == 0) {
            if (lastCleanDay != d) {
                lastCleanDay = d;
                try { truncate(new File("/sdcard/TouchSprite/log/cancel_attack.log")); } catch (Exception ignored) {}
                try { truncate(new File("/sdcard/TouchSprite/log/error.log")); } catch (Exception ignored) {}
                try { truncate(new File("/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/log/cancel_attack.log")); } catch (Exception ignored) {}
                try { truncate(new File("/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/log/error.log")); } catch (Exception ignored) {}
            }
        }
    }
    private static void truncate(File f) throws Exception {
        if (f != null && f.exists()) {
            FileOutputStream fos = new FileOutputStream(f, false);
            fos.flush();
            fos.close();
        }
    }
}
