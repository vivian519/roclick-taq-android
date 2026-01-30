package com.iceflow.roclicktaq;

public class App extends android.app.Application {
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("Uncaught ").append(e.getClass().getName()).append(" ").append(e.getMessage());
                StackTraceElement[] st = e.getStackTrace();
                int n = Math.min(st.length, 30);
                for (int i = 0; i < n; i++) {
                    sb.append("\n at ").append(st[i].toString());
                }
                com.iceflow.roclicktaq.io.LogIO.write("error.log", sb.toString());
            } catch (Exception ignored) {}
        });
    }
}
