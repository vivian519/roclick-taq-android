package com.iceflow.roclicktaq.io;

import android.content.Context;
import android.os.Environment;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigIO {
    public static final String CONFIG_PATH = "/sdcard/TouchSprite/config/cancel_attack.json";
    public static final String CONFIG_PATH2 = "/sdcard/Android/data/com.touchsprite.android/files/TouchSprite/config/cancel_attack.json";

    public static JSONObject defaultConfig() {
        JSONObject j = new JSONObject();
        try {
            j.put("mode", "image");
            j.put("img", "/sdcard/TouchSprite/lua/auto.png");
            j.put("img_sim", 0.85);
            j.put("interval_ms", 500);
            j.put("blue_ratio", 0.6);
            j.put("sample_step", 6);
            j.put("confirm_ms", 800);
            j.put("baseW", 1080);
            j.put("baseH", 2280);
            j.put("rect_left", 260);
            j.put("rect_right", 42);
            j.put("rect_top", 270);
            j.put("rect_bottom", 40);
        } catch (JSONException e) {
        }
        return j;
    }

    public static boolean canManageAllFiles(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return true;
        }
    }

    public static JSONObject readConfig() {
        File f1 = new File(CONFIG_PATH);
        if (f1.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f1);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return new JSONObject(sb.toString());
            } catch (Exception e) {
            }
        }
        File f2 = new File(CONFIG_PATH2);
        if (f2.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f2);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return new JSONObject(sb.toString());
            } catch (Exception e) {
            }
        }
        return defaultConfig();
    }

    public static boolean writeConfig(JSONObject j) {
        try {
            File f1 = new File(CONFIG_PATH);
            File d1 = f1.getParentFile();
            if (d1 != null && !d1.exists()) {
                d1.mkdirs();
            }
            FileOutputStream fos1 = new FileOutputStream(f1, false);
            fos1.write(j.toString().getBytes(StandardCharsets.UTF_8));
            fos1.flush();
            fos1.close();
            return true;
        } catch (Exception e1) {
            try {
                File f2 = new File(CONFIG_PATH2);
                File d2 = f2.getParentFile();
                if (d2 != null && !d2.exists()) {
                    d2.mkdirs();
                }
                FileOutputStream fos2 = new FileOutputStream(f2, false);
                fos2.write(j.toString().getBytes(StandardCharsets.UTF_8));
                fos2.flush();
                fos2.close();
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }
}
