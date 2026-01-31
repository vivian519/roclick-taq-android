package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.io.ConfigIO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BlackPointsSettingsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_points_settings);
        ListView list = findViewById(R.id.list_points);
        Button btnClear = findViewById(R.id.btn_clear);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, loadPoints());
        list.setAdapter(adapter);
        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject cfg = ConfigIO.readConfig();
                cfg.remove("fixed_black_points");
                cfg.remove("fixed_black_cx");
                cfg.remove("fixed_black_cy");
                boolean ok = ConfigIO.writeConfig(cfg);
                if (ok) {
                    ArrayAdapter<String> na = new ArrayAdapter<>(BlackPointsSettingsActivity.this, android.R.layout.simple_list_item_1, loadPoints());
                    list.setAdapter(na);
                    Toast.makeText(BlackPointsSettingsActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BlackPointsSettingsActivity.this, "清空失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ArrayList<String> loadPoints() {
        ArrayList<String> items = new ArrayList<>();
        JSONObject cfg = ConfigIO.readConfig();
        JSONArray arr = cfg.optJSONArray("fixed_black_points");
        if (arr != null && arr.length() > 0) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) {
                    int cx = o.optInt("x", o.optInt("cx", 0));
                    int cy = o.optInt("y", o.optInt("cy", 0));
                    items.add(cx + "," + cy);
                }
            }
        } else {
            if (cfg.has("fixed_black_cx") && cfg.has("fixed_black_cy")) {
                items.add(cfg.optInt("fixed_black_cx") + "," + cfg.optInt("fixed_black_cy"));
            } else {
                items.add("暂无坐标");
            }
        }
        return items;
    }
}
