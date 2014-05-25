package ru.gelin.android.countdown;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;

public class MainActivity extends Activity implements View.OnSystemUiVisibilityChangeListener {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initWheel(R.id.ten_mins, 0, 9);
        initWheel(R.id.mins, 0, 9);
        initWheel(R.id.ten_secs, 0, 5);
        initWheel(R.id.secs, 0, 9);

        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        content.setOnSystemUiVisibilityChangeListener(this);
    }

    void initWheel(int id, int min, int max) {
        AbstractWheel wheel = (AbstractWheel)findViewById(id);
        NumericWheelAdapter adapter = new NumericWheelAdapter(this, min, max);
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        wheel.setViewAdapter(adapter);
        wheel.setCyclic(true);
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
}
