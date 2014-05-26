package ru.gelin.android.countdown;

import android.content.Context;

/**
 *  A timer to count down and up.
 *
 *  http://plantuml.com/plantuml/png/POun3i8m34NtdC8Z8FK27JW4i266YZZ8Kd6Ys05nUvnGqGW6U_X-_t-BJefmr9XSNIESw8uPI82ZGYxCn3Vdf667miwcWwwni3RYlBl0CEnR58pmd0HInWV58lCBnDI4_ajPoZzaRQvzqI5Md7MpNOkgtFNKuFwy-DSxLZSnAKqpn11C-6KoVYYD_JSU0p7wnVC6
 *
 */
public class Timer {

    public enum State {
        STOP, RUN;
    }

    /** State of the timer */
    State state = State.STOP;

    /** Point of zero. As timestamp. */
    long zeroTime;

    /** Current offset in Stop state, in seconds */
    long offset;

    /** Initial offset, where to reset, in seconds */
    long initOffset;

    /**
     *  Constructs the timer for the application from the context.
     *  Loads from SharedPreferences.
     */
    Timer(Context context) {
        //TODO
    }

    /**
     *  Saves the timer to the application context.
     *  Saves to SharedPreferences.
     */
    void save() {
        //TODO
    }

    /**
     *  Starts the timer if it was stopped.
     */
    public synchronized void start() {
        if (State.RUN.equals(this.state)) {
            return;
        }
        updateZeroTime();
        this.state = State.RUN;
    }

    private void updateZeroTime() {
        long now = System.currentTimeMillis();
        this.zeroTime = now + this.offset * 1000;
    }

    /**
     *  Stops the timer if it was run.
     */
    public synchronized void stop() {
        if (State.STOP.equals(this.state)) {
            return;
        }
        this.offset = findOffset();
        this.state = State.STOP;
    }

    private long findOffset() {
        long now = System.currentTimeMillis();
        return (now - this.zeroTime) / 1000;
    }

    /**
     *  Sets the initial offset of the timer.
     *  @param offset   initial time offset in seconds
     */
    public synchronized void set(long offset) {
        this.initOffset = offset;
    }

    /**
     *  Resets the timer offset to the initial offset.
     *  If timer is stopped, just moves the current offset.
     *  If timer is run, shifts the zero time.
     */
    public synchronized void reset() {
        this.offset = this.initOffset;
        if (State.RUN.equals(this.state)) {
            updateZeroTime();
        }
    }

    /**
     *  Returns the current offset.
     *  Negative offset means countdown till zero time. Positive offset means countup after zero time.
     *  If the timer is stopped the offset doesn't change (only by #set() or #reset()).
     *  If the timer is run, the offset changes according to the current time.
     */
    public synchronized long getOffset() {
        switch (this.state) {
            case STOP:
                return this.offset;
            case RUN:
                return findOffset();
        }
        return 0l;
    }

    /**
     *  Returns the timer state.
     */
    public synchronized State getState() {
        return this.state;
    }

    /**
     *  Returns true if the time is running.
     */
    public synchronized boolean isRunning() {
        return State.RUN.equals(this.state);
    }

}
