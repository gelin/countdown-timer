package ru.gelin.android.countdown;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;


public class MainActivity extends Activity implements View.OnSystemUiVisibilityChangeListener, OnWheelChangedListener {

    static final int MAX_OFFSET = 99 * 60 + 59;

    static final Typeface WHEEL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    static final int WHEEL_COLOR = 0xffeeeeee;
    static final int WHEEL_COLOR_RED = 0xffee0000;

    Timer timer;
    UpdateTask updater;
    float wheelTextSize;
    int wheelsColor = WHEEL_COLOR;
    AbstractWheel wheels[] = new AbstractWheel[4];

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.timer = new Timer(this);
        setContentView(R.layout.main);

        View content = findViewById(android.R.id.content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        content.setOnSystemUiVisibilityChangeListener(this);

        Display display = getWindowManager().getDefaultDisplay();
        TypedValue ratio = new TypedValue();
        getResources().getValue(R.dimen.wheel_text_size_ratio, ratio, true);
        this.wheelTextSize = display.getHeight() * ratio.getFloat();
        Log.d(Tag.TAG, String.format("text size: %f (ratio: %f)", this.wheelTextSize, ratio.getFloat()));
//        Log.d(Tag.TAG, "widthxheight: " + getWindowManager().getDefaultDisplay().getWidth() + "x" + getWindowManager().getDefaultDisplay().getHeight());

        this.wheels[0] = (AbstractWheel) findViewById(R.id.ten_mins);
        this.wheels[1] = (AbstractWheel) findViewById(R.id.mins);
        this.wheels[2] = (AbstractWheel) findViewById(R.id.ten_secs);
        this.wheels[3] = (AbstractWheel) findViewById(R.id.secs);

        initWheel(this.wheels[0], 0, 9);
        initWheel(this.wheels[1], 0, 9);
        initWheel(this.wheels[2], 0, 5);
        initWheel(this.wheels[3], 0, 9);
    }

    void initWheel(AbstractWheel wheel, int min, int max) {
        NumericWheelAdapter adapter = new NumericWheelAdapter(this, min, max);
//        Log.d(Tag.TAG, "text size: " + this.wheelTextSize);
        adapter.setTextSizeUnit(TypedValue.COMPLEX_UNIT_PX);
        adapter.setTextSize(this.wheelTextSize);
        adapter.setTextColor(WHEEL_COLOR);
        adapter.setTextTypeface(WHEEL_TYPEFACE);
        wheel.setViewAdapter(adapter);
        wheel.setCyclic(true);
        wheel.setVisibleItems(1);
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
        new UpdateTask(true).execute();
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
        disableWheels();
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
        enableWheels();
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

    void enableWheels() {
        for (AbstractWheel wheel : this.wheels) {
            wheel.setEnabled(true);
        }
    }

    void disableWheels() {
        for (AbstractWheel wheel : this.wheels) {
            wheel.setEnabled(false);
        }
    }

    void updateWheels() {
//        Log.d(Tag.TAG, "updating");

        int origOffset = this.timer.getOffset();
        changeWheelsColor(origOffset > 0 ? WHEEL_COLOR_RED : WHEEL_COLOR);

        int absOffset = Math.abs(origOffset);
        int offset;
        if (absOffset > MAX_OFFSET) {
            offset = MAX_OFFSET;
        } else {
            offset = absOffset;
        }
        int mins = offset / 60;
        int secs = offset % 60;

//        Log.d(Tag.TAG, String.format("%d mins, %d secs", mins, secs));
        updateWheel(this.wheels[0], mins / 10);
        updateWheel(this.wheels[1], mins % 10);
        updateWheel(this.wheels[2], secs / 10);
        updateWheel(this.wheels[3], secs % 10);

//        Log.d(Tag.TAG, "updated");
    }

    void updateWheel(AbstractWheel wheel, int value) {
        wheel.setCurrentItem(value, true, false);
    }

    void changeWheelsColor(int color) {
        if (this.wheelsColor == color) {
            return;
        }
        for (AbstractWheel wheel : this.wheels) {
            changeWheelColor(wheel, color);
        }
        this.wheelsColor = color;
    }

    void changeWheelColor(AbstractWheel wheel, int color) {
        NumericWheelAdapter adapter = (NumericWheelAdapter)wheel.getViewAdapter();
        adapter.setTextColor(color);
        wheel.setViewAdapter(adapter);  //to force view redraw
    }

    class UpdateTask extends AsyncTask<Void, Void, Void> {

        boolean run = true;

        boolean once = false;
        public void stop() {
            this.run = false;
        }

        public UpdateTask() {
            this(false);
        }

        public UpdateTask(boolean once) {
            this.once = once;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (this.once) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //nothing to do
                }
                publishProgress();
                return null;
            }
            while(this.run) {
                publishProgress();
                try {
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
    public void onChanged(AbstractWheel changedWheel, int oldValue, int newValue) {
        if (changedWheel.isEnabled() == false) {
            return;
        }
        if (this.timer.isRunning()) {
            return;
        }
//        Log.d(Tag.TAG, "changed: " + newValue);
        int mins = this.wheels[0].getCurrentItem() * 10 + this.wheels[1].getCurrentItem();
        int secs = this.wheels[2].getCurrentItem() * 10 + this.wheels[3].getCurrentItem();
        this.timer.set(-(mins * 60 + secs));
        this.timer.reset();
        changeWheelsColor(WHEEL_COLOR);
    }

}
