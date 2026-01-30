package com.iceflow.roclicktaq.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.iceflow.roclicktaq.R;
import com.iceflow.roclicktaq.io.ConfigIO;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends Activity {
    private Spinner spMode;
    private EditText etImg, etSim, etInterval, etBlueRatio, etSampleStep, etConfirmMs, etBaseW, etBaseH, etRectLeft, etRectRight, etRectTop, etRectBottom;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        spMode = findViewById(R.id.sp_mode);
        etImg = findViewById(R.id.et_img);
        etSim = findViewById(R.id.et_sim);
        etInterval = findViewById(R.id.et_interval);
        etBlueRatio = findViewById(R.id.et_blue_ratio);
        etSampleStep = findViewById(R.id.et_sample_step);
        etConfirmMs = findViewById(R.id.et_confirm_ms);
        etBaseW = findViewById(R.id.et_base_w);
        etBaseH = findViewById(R.id.et_base_h);
        etRectLeft = findViewById(R.id.et_rect_left);
        etRectRight = findViewById(R.id.et_rect_right);
        etRectTop = findViewById(R.id.et_rect_top);
        etRectBottom = findViewById(R.id.et_rect_bottom);
        Button btnLoad = findViewById(R.id.btn_load);
        Button btnSave = findViewById(R.id.btn_save);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject j = ConfigIO.readConfig();
                applyToUI(j);
                Toast.makeText(SettingsActivity.this, "已加载配置", Toast.LENGTH_SHORT).show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject j = collectFromUI();
                boolean ok = ConfigIO.writeConfig(j);
                if (ok) {
                    Toast.makeText(SettingsActivity.this, "已保存配置", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        applyToUI(ConfigIO.defaultConfig());
    }

    private void applyToUI(JSONObject j) {
        try {
            String mode = j.optString("mode", "image");
            int idx = "color".equalsIgnoreCase(mode) ? 1 : 0;
            spMode.setSelection(idx);
            etImg.setText(j.optString("img", "/sdcard/TouchSprite/lua/auto.png"));
            etSim.setText(String.valueOf(j.optDouble("img_sim", 0.85)));
            etInterval.setText(String.valueOf(j.optInt("interval_ms", 500)));
            etBlueRatio.setText(String.valueOf(j.optDouble("blue_ratio", 0.6)));
            etSampleStep.setText(String.valueOf(j.optInt("sample_step", 6)));
            etConfirmMs.setText(String.valueOf(j.optInt("confirm_ms", 800)));
            etBaseW.setText(String.valueOf(j.optInt("baseW", 1080)));
            etBaseH.setText(String.valueOf(j.optInt("baseH", 2280)));
            etRectLeft.setText(String.valueOf(j.optInt("rect_left", 260)));
            etRectRight.setText(String.valueOf(j.optInt("rect_right", 42)));
            etRectTop.setText(String.valueOf(j.optInt("rect_top", 270)));
            etRectBottom.setText(String.valueOf(j.optInt("rect_bottom", 40)));
        } catch (Exception e) {
        }
    }

    private JSONObject collectFromUI() {
        JSONObject j = new JSONObject();
        try {
            String mode = spMode.getSelectedItemPosition() == 1 ? "color" : "image";
            j.put("mode", mode);
            j.put("img", text(etImg, "/sdcard/TouchSprite/lua/auto.png"));
            j.put("img_sim", parseDouble(etSim, 0.85));
            j.put("interval_ms", parseInt(etInterval, 500));
            j.put("blue_ratio", parseDouble(etBlueRatio, 0.6));
            j.put("sample_step", parseInt(etSampleStep, 6));
            j.put("confirm_ms", parseInt(etConfirmMs, 800));
            j.put("baseW", parseInt(etBaseW, 1080));
            j.put("baseH", parseInt(etBaseH, 2280));
            j.put("rect_left", parseInt(etRectLeft, 260));
            j.put("rect_right", parseInt(etRectRight, 42));
            j.put("rect_top", parseInt(etRectTop, 270));
            j.put("rect_bottom", parseInt(etRectBottom, 40));
        } catch (JSONException e) {
        }
        return j;
    }

    private String text(EditText et, String def) {
        String s = et.getText().toString();
        if (TextUtils.isEmpty(s)) return def;
        return s;
    }

    private int parseInt(EditText et, int def) {
        try {
            String s = et.getText().toString();
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private double parseDouble(EditText et, double def) {
        try {
            String s = et.getText().toString();
            return Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }
}
