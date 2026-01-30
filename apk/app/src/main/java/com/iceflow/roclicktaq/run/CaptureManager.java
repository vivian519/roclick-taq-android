package com.iceflow.roclicktaq.run;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;

import com.iceflow.roclicktaq.io.LogIO;

import java.nio.ByteBuffer;

public class CaptureManager {
    private MediaProjection projection;
    private ImageReader reader;
    private VirtualDisplay virtualDisplay;
    private int w;
    private int h;
    public static final int REQ_CODE = 9527;

    public Intent createIntent(Context c) {
        MediaProjectionManager m = (MediaProjectionManager) c.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        return m.createScreenCaptureIntent();
    }

    public boolean start(Context c, int resultCode, Intent data) {
        try {
            try { LogIO.write("error.log", "请求录屏 rc=" + resultCode + " data=" + (data != null)); } catch (Exception ignored) {}
            MediaProjectionManager m = (MediaProjectionManager) c.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            projection = m.getMediaProjection(resultCode, data);
            if (projection == null) {
                try { LogIO.write("error.log", "MediaProjection为空"); } catch (Exception ignored) {}
                return false;
            }
            try { Thread.sleep(200); } catch (Exception ignored) {}
            DisplayMetrics dm = c.getResources().getDisplayMetrics();
            w = dm.widthPixels & ~1;
            h = dm.heightPixels & ~1;
            try {
                reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2);
                virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);
                boolean hasFrame = false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Image img = reader.acquireLatestImage();
                        if (img != null) { img.close(); hasFrame = true; break; }
                    } catch (Exception ignored) {}
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                }
                if (hasFrame) return true;
                try { LogIO.write("error.log", "VD无首帧 AUTO_MIRROR w=" + w + " h=" + h + " dpi=" + dm.densityDpi); } catch (Exception ignored) {}
                try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
                virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, reader.getSurface(), null, null);
                hasFrame = false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Image img = reader.acquireLatestImage();
                        if (img != null) { img.close(); hasFrame = true; break; }
                    } catch (Exception ignored) {}
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                }
                if (hasFrame) return true;
                try { LogIO.write("error.log", "VD无首帧 PRESENTATION"); } catch (Exception ignored) {}
                try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
                virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY, reader.getSurface(), null, null);
                hasFrame = false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Image img = reader.acquireLatestImage();
                        if (img != null) { img.close(); hasFrame = true; break; }
                    } catch (Exception ignored) {}
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                }
                if (hasFrame) return true;
                try { LogIO.write("error.log", "VD无首帧 OWN_CONTENT_ONLY，尝试降分辨率"); } catch (Exception ignored) {}
                int w2 = Math.max(480, (w / 2) & ~1);
                int h2 = Math.max(800, (h / 2) & ~1);
                try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
                try { if (reader != null) reader.close(); } catch (Exception ignored) {}
                reader = ImageReader.newInstance(w2, h2, PixelFormat.RGBA_8888, 2);
                w = w2; h = h2;
                virtualDisplay = projection.createVirtualDisplay("cap", w2, h2, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);
                hasFrame = false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Image img = reader.acquireLatestImage();
                        if (img != null) { img.close(); hasFrame = true; break; }
                    } catch (Exception ignored) {}
                    try { Thread.sleep(100); } catch (Exception ignored) {}
                }
                if (hasFrame) {
                    try { LogIO.write("error.log", "VD降分辨率成功 w=" + w2 + " h=" + h2); } catch (Exception ignored) {}
                    return true;
                }
                try { LogIO.write("error.log", "VD降分辨率仍无首帧"); } catch (Exception ignored) {}
                return false;
            } catch (Exception e1) {
                try { LogIO.write("error.log", "VD失败 AUTO_MIRROR w=" + w + " h=" + h + " dpi=" + dm.densityDpi + " ex=" + e1.getClass().getName() + " msg=" + e1.getMessage()); } catch (Exception ignored) {}
                try {
                    virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, reader.getSurface(), null, null);
                    boolean hasFrame = false;
                    for (int i = 0; i < 10; i++) {
                        try {
                            Image img = reader.acquireLatestImage();
                            if (img != null) { img.close(); hasFrame = true; break; }
                        } catch (Exception ignored) {}
                        try { Thread.sleep(100); } catch (Exception ignored) {}
                    }
                    if (hasFrame) return true;
                    try { LogIO.write("error.log", "VD无首帧 PRESENTATION"); } catch (Exception ignored) {}
                    try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
                    try {
                        virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY, reader.getSurface(), null, null);
                        boolean hasFrame2 = false;
                        for (int i = 0; i < 10; i++) {
                            try {
                                Image img = reader.acquireLatestImage();
                                if (img != null) { img.close(); hasFrame2 = true; break; }
                            } catch (Exception ignored) {}
                            try { Thread.sleep(100); } catch (Exception ignored) {}
                        }
                        if (hasFrame2) return true;
                        try { LogIO.write("error.log", "VD无首帧 OWN_CONTENT_ONLY，尝试降分辨率"); } catch (Exception ignored) {}
                        int w2 = Math.max(480, (w / 2) & ~1);
                        int h2 = Math.max(800, (h / 2) & ~1);
                        try { if (virtualDisplay != null) virtualDisplay.release(); } catch (Exception ignored) {}
                        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
                        reader = ImageReader.newInstance(w2, h2, PixelFormat.RGBA_8888, 2);
                        w = w2; h = h2;
                        virtualDisplay = projection.createVirtualDisplay("cap", w2, h2, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);
                        boolean hasFrame3 = false;
                        for (int i = 0; i < 10; i++) {
                            try {
                                Image img = reader.acquireLatestImage();
                                if (img != null) { img.close(); hasFrame3 = true; break; }
                            } catch (Exception ignored) {}
                            try { Thread.sleep(100); } catch (Exception ignored) {}
                        }
                        if (hasFrame3) {
                            try { LogIO.write("error.log", "VD降分辨率成功 w=" + w2 + " h=" + h2); } catch (Exception ignored) {}
                            return true;
                        }
                        try { LogIO.write("error.log", "VD降分辨率仍无首帧"); } catch (Exception ignored) {}
                        return false;
                    } catch (Exception e3) {
                        try { LogIO.write("error.log", "VD失败 OWN_CONTENT_ONLY，尝试降分辨率 ex=" + e3.getClass().getName() + " msg=" + e3.getMessage()); } catch (Exception ignored) {}
                        try {
                            int w2 = Math.max(480, (w / 2) & ~1);
                            int h2 = Math.max(800, (h / 2) & ~1);
                            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
                            reader = ImageReader.newInstance(w2, h2, PixelFormat.RGBA_8888, 2);
                            w = w2; h = h2;
                            virtualDisplay = projection.createVirtualDisplay("cap", w2, h2, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);
                            boolean hasFrame4 = false;
                            for (int i = 0; i < 10; i++) {
                                try {
                                    Image img = reader.acquireLatestImage();
                                    if (img != null) { img.close(); hasFrame4 = true; break; }
                                } catch (Exception ignored) {}
                                try { Thread.sleep(100); } catch (Exception ignored) {}
                            }
                            if (hasFrame4) {
                                try { LogIO.write("error.log", "VD降分辨率成功 w=" + w2 + " h=" + h2); } catch (Exception ignored) {}
                                return true;
                            }
                            try { LogIO.write("error.log", "VD降分辨率仍无首帧"); } catch (Exception ignored) {}
                            return false;
                        } catch (Exception e4) {
                            try { LogIO.write("error.log", "VD降分辨率仍失败 ex=" + e4.getClass().getName() + " msg=" + e4.getMessage()); } catch (Exception ignored) {}
                            return false;
                        }
                    }
                } catch (Exception e2) {
                    try { LogIO.write("error.log", "VD失败 PRESENTATION ex=" + e2.getClass().getName() + " msg=" + e2.getMessage()); } catch (Exception ignored) {}
                    try {
                        virtualDisplay = projection.createVirtualDisplay("cap", w, h, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY, reader.getSurface(), null, null);
                        boolean hasFrame = false;
                        for (int i = 0; i < 10; i++) {
                            try {
                                Image img = reader.acquireLatestImage();
                                if (img != null) { img.close(); hasFrame = true; break; }
                            } catch (Exception ignored) {}
                            try { Thread.sleep(100); } catch (Exception ignored) {}
                        }
                        if (hasFrame) return true;
                        try { LogIO.write("error.log", "VD无首帧 OWN_CONTENT_ONLY，尝试降分辨率"); } catch (Exception ignored) {}
                    } catch (Exception e3) {
                        try { LogIO.write("error.log", "VD失败 OWN_CONTENT_ONLY，尝试降分辨率 ex=" + e3.getClass().getName() + " msg=" + e3.getMessage()); } catch (Exception ignored) {}
                        try {
                            int w2 = Math.max(480, (w / 2) & ~1);
                            int h2 = Math.max(800, (h / 2) & ~1);
                            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
                            reader = ImageReader.newInstance(w2, h2, PixelFormat.RGBA_8888, 2);
                            w = w2; h = h2;
                            virtualDisplay = projection.createVirtualDisplay("cap", w2, h2, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);
                            boolean hasFrame = false;
                            for (int i = 0; i < 10; i++) {
                                try {
                                    Image img = reader.acquireLatestImage();
                                    if (img != null) { img.close(); hasFrame = true; break; }
                                } catch (Exception ignored) {}
                                try { Thread.sleep(100); } catch (Exception ignored) {}
                            }
                            if (hasFrame) {
                                try { LogIO.write("error.log", "VD降分辨率成功 w=" + w2 + " h=" + h2); } catch (Exception ignored) {}
                                return true;
                            }
                            try { LogIO.write("error.log", "VD降分辨率仍无首帧"); } catch (Exception ignored) {}
                            return false;
                        } catch (Exception e4) {
                            try { LogIO.write("error.log", "VD降分辨率仍失败 ex=" + e4.getClass().getName() + " msg=" + e4.getMessage()); } catch (Exception ignored) {}
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            try { LogIO.write("error.log", "MediaProjection启动失败 ex=" + e.getClass().getName() + " msg=" + e.getMessage()); } catch (Exception ignored) {}
            return false;
        }
        return false;
    }

    public Bitmap capture() {
        try {
            Image img = reader.acquireLatestImage();
            if (img == null) return null;
            Image.Plane[] planes = img.getPlanes();
            ByteBuffer buf = planes[0].getBuffer();
            int rowStride = planes[0].getRowStride();
            int pixelStride = planes[0].getPixelStride();
            int bmpW = w;
            int bmpH = h;
            Bitmap bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[bmpW * bmpH];
            int rowBytes = bmpW * pixelStride;
            byte[] row = new byte[rowBytes];
            for (int y = 0; y < bmpH; y++) {
                int off = y * rowStride;
                buf.position(off);
                buf.get(row, 0, rowBytes);
                int xMax = bmpW;
                for (int x = 0; x < xMax; x++) {
                    int i = x * pixelStride;
                    int a = row[i + 3] & 0xff;
                    int r = row[i] & 0xff;
                    int g = row[i + 1] & 0xff;
                    int b = row[i + 2] & 0xff;
                    pixels[y * bmpW + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            bmp.setPixels(pixels, 0, bmpW, 0, 0, bmpW, bmpH);
            img.close();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public int getW() { return w; }
    public int getH() { return h; }
    public void stop() {
        try {
            if (virtualDisplay != null) virtualDisplay.release();
            if (reader != null) reader.close();
            if (projection != null) projection.stop();
        } catch (Exception e) {
        }
    }
}
