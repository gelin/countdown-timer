package ru.gelin.android.countdown;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;

public class MainActivity extends Activity implements View.OnSystemUiVisibilityChangeListener, OnWheelChangedListener {

    static final int MAX_OFFSET = 99 * 60 + 59;

    Timer timer;
    UpdateTask updater;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.timer = new Timer(this);
        setContentView(R.layout.main);

        initWheel(R.id.ten_mins, 0, 9);
        initWheel(R.id.mins, 0, 9);
        initWheel(R.id.ten_secs, 0, 5);
        initWheel(R.id.secs, 0, 9);

        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        content.setOnSystemUiVisibilityChangeListener(this);

        //TODO remove test
        this.timer.set(-30);
        this.timer.reset();
    }

    void initWheel(int id, int min, int max) {
        AbstractWheel wheel = (AbstractWheel)findViewById(id);
        NumericWheelAdapter adapter = new NumericWheelAdapter(this, min, max);
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        wheel.setViewAdapter(adapter);
        wheel.setCyclic(true);
        wheel.addChangingListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWheels();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    public void start(View btn) {
        enableWheel(R.id.ten_mins, false);
        enableWheel(R.id.mins, false);
        enableWheel(R.id.ten_secs, false);
        enableWheel(R.id.secs, false);
        this.timer.start();
        this.updater = new UpdateTask();
        this.updater.execute();
        findViewById(R.id.start_btn).setVisibility(View.GONE);
        findViewById(R.id.stop_btn).setVisibility(View.VISIBLE);
    }

    public void stop(View btn) {
        this.timer.stop();
        this.updater.stop();
        enableWheel(R.id.ten_mins, true);
        enableWheel(R.id.mins, true);
        enableWheel(R.id.ten_secs, true);
        enableWheel(R.id.secs, true);
        findViewById(R.id.stop_btn).setVisibility(View.GONE);
        findViewById(R.id.start_btn).setVisibility(View.VISIBLE);
    }

    public void reset(View btn) {
        this.timer.reset();
        updateWheels();
    }

    void enableWheel(int id, boolean enabled) {
        AbstractWheel wheel = (AbstractWheel)findViewById(id);
        wheel.setEnabled(enabled);
    }

    void updateWheels() {
        long doffset = Math.abs(this.timer.getOffset());
        int offset;
        if (doffset > MAX_OFFSET) {
            offset = MAX_OFFSET;
        } else {
            offset = (int)doffset;
        }
        int mins = offset / 60;
        int secs = offset % 60;

        Log.d(Tag.TAG, String.format("%d mins, %d secs", mins, secs));
        updateWheel(R.id.ten_mins, mins / 10);
        updateWheel(R.id.mins, mins % 10);
        updateWheel(R.id.ten_secs, secs / 10);
        updateWheel(R.id.secs, secs % 10);
    }

    void updateWheel(int id, int value) {
        AbstractWheel wheel = (AbstractWheel)findViewById(id);
        wheel.setCurrentItem(value, true);
    }

    class UpdateTask extends AsyncTask<Void, Void, Void> {

        boolean run = true;

        public void stop() {
            this.run = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while(this.run) {
                try {
                    publishProgress();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //nothing to do
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            updateWheels();
        }

    }

    @Override
    public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
        if (this.timer.isRunning()) {
            return;
        }
        int mins = getWheelValue(R.id.ten_mins) * 10 + getWheelValue(R.id.mins);
        int secs = getWheelValue(R.id.ten_secs) * 10 + getWheelValue(R.id.secs);
        this.timer.set(-(mins * 60 + secs));
        this.timer.reset();
    }

    int getWheelValue(int id) {
        AbstractWheel wheel = (AbstractWheel)findViewById(id);
        return wheel.getCurrentItem();
    }

}
