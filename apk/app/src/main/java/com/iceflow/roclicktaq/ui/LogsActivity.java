package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.iceflow.roclicktaq.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LogsActivity extends Activity {
    private Spinner spFile;
    private TextView tvContent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        spFile = findViewById(R.id.sp_log_file);
        tvContent = findViewById(R.id.tv_log_content);
        Button btnRefresh = findViewById(R.id.btn_refresh);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"cancel_attack.log", "error.log", "ts.log", "runScript"});
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFile.setAdapter(ad);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refresh();
            }
        });
        refresh();
    }

    private void refresh() {
        String name = spFile.getSelectedItem().toString();
        String path = "/sdcard/TouchSprite/log/" + name;
        tvContent.setText(readFile(path));
    }

    private String readFile(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return "";
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
