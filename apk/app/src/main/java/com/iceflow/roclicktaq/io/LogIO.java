package com.iceflow.roclicktaq.io;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogIO {
    public static String logDir() {
        return "/sdcard/TouchSprite/log";
    }
    public static void write(String name, String msg) {
        try {
            File dir = new File(logDir());
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, name);
            FileOutputStream fos = new FileOutputStream(f, true);
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            fos.write((ts + " " + msg + "\n").getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        } catch (Exception e) {
        }
    }
}
