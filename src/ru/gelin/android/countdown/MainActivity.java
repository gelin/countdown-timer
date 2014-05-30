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
    boolean redWheels = false;
    AbstractWheel wheels[] = new AbstractWheel[4];

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.timer = new Timer(this);
        setContentView(R.layout.main);

        this.wheels[0] = (AbstractWheel) findViewById(R.id.ten_mins);
        this.wheels[1] = (AbstractWheel) findViewById(R.id.mins);
        this.wheels[2] = (AbstractWheel) findViewById(R.id.ten_secs);
        this.wheels[3] = (AbstractWheel) findViewById(R.id.secs);
        initWheel(this.wheels[0], 0, 9);
        initWheel(this.wheels[1], 0, 9);
        initWheel(this.wheels[2], 0, 5);
        initWheel(this.wheels[3], 0, 9);

        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        content.setOnSystemUiVisibilityChangeListener(this);
    }

    void initWheel(AbstractWheel wheel, int min, int max) {
        NumericWheelAdapter adapter = new NumericWheelAdapter(this, min, max);
        adapter.setItemResource(R.layout.wheel_text);
        adapter.setItemTextResource(R.id.text);
        wheel.setViewAdapter(adapter);
        wheel.setCyclic(true);
        wheel.addChangingListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.timer.isRunning()) {
            start();
        } else {
            stop();
        }
        updateWheels();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.timer.save();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    public void start(View btn) {
        start();
    }

    void start() {
        for (AbstractWheel wheel : this.wheels) {
            wheel.setEnabled(false);
        }
        this.timer.start();
        this.updater = new UpdateTask();
        this.updater.execute();
        findViewById(R.id.start_btn).setVisibility(View.GONE);
        findViewById(R.id.stop_btn).setVisibility(View.VISIBLE);
    }

    public void stop(View btn) {
        stop();
    }

    void stop() {
        this.timer.stop();
        if (this.updater != null) {
            this.updater.stop();
        }
        for (AbstractWheel wheel : this.wheels) {
            changeWheelLayout(wheel, R.layout.wheel_text);
            wheel.setEnabled(true);
        }
        this.redWheels = false;
        findViewById(R.id.stop_btn).setVisibility(View.GONE);
        findViewById(R.id.start_btn).setVisibility(View.VISIBLE);
    }

    public void reset(View btn) {
        reset();
    }

    void reset() {
        this.timer.reset();
        updateWheels();
    }

    void updateWheels() {
        int origOffset = this.timer.getOffset();

        if (origOffset > 0 ^ this.redWheels) {
            this.redWheels = origOffset > 0;
            int layout = this.redWheels ? R.layout.wheel_text_red : R.layout.wheel_text;
            for (AbstractWheel wheel : this.wheels) {
                changeWheelLayout(wheel, layout);
            }
        }

        int absOffset = Math.abs(origOffset);
        int offset;
        if (absOffset > MAX_OFFSET) {
            offset = MAX_OFFSET;
        } else {
            offset = absOffset;
        }
        int mins = offset / 60;
        int secs = offset % 60;

        Log.d(Tag.TAG, String.format("%d mins, %d secs", mins, secs));
        updateWheel(this.wheels[0], mins / 10);
        updateWheel(this.wheels[1], mins % 10);
        updateWheel(this.wheels[2], secs / 10);
        updateWheel(this.wheels[3], secs % 10);
    }

    void changeWheelLayout(AbstractWheel wheel, int layout) {
        NumericWheelAdapter adapter = (NumericWheelAdapter)wheel.getViewAdapter();
        adapter.setItemResource(layout);
        wheel.setViewAdapter(adapter);  //to force view redraw
    }

    void updateWheel(AbstractWheel wheel, int value) {
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
