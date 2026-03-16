package ru.gelin.android.countdown

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * A timer to count down and up.
 *
 * @startuml
 * state Stop
 * Stop: Timer is not running
 *
 * state Run
 * Run: Timer is counting
 *
 * [*] --> Stop : was previously stopped
 * [*] --> Run : was previously run
 *
 * Stop --> Run : start() counting
 * Run --> Stop : stop() counting
 *
 * Stop -> Stop : set() initial offset
 * Stop -> Stop : reset() to previously defined offset
 * @enduml
 */
class Timer(context: Context) {

    enum class State {
        STOP, RUN
    }

    /** State of the timer  */
    var state = State.STOP
        private set

    /** Point of zero. As timestamp.  */
    private var zeroTime: Long = 0

    /** Current offset in Stop state, in seconds  */
    private var offset: Int = 0

    /** Initial offset, where to reset, in seconds  */
    private var initOffset: Int = 0

    /** Preferences to save the state  */
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val STATE_PREF = "timer_state"
        const val ZERO_TIME_PREF = "timer_zero_time"
        const val OFFSET_PREF = "timer_offset"
        const val INIT_OFFSET_PREF = "timer_init_offset"
    }

    /**
     * Constructs the timer for the application from the context.
     * Loads from SharedPreferences.
     */
    init {
        this.state = State.valueOf(this.prefs.getString(STATE_PREF, State.STOP.toString()) ?: State.STOP.toString())
        this.zeroTime = this.prefs.getLong(ZERO_TIME_PREF, System.currentTimeMillis())
        this.offset = this.prefs.getInt(OFFSET_PREF, 0)
        this.initOffset = this.prefs.getInt(INIT_OFFSET_PREF, 0)
    }

    /**
     * Saves the timer to the application context.
     * Saves to SharedPreferences.
     */
    fun save() {
        val editor = this.prefs.edit()
        editor.putString(STATE_PREF, this.state.toString())
        editor.putLong(ZERO_TIME_PREF, this.zeroTime)
        editor.putInt(OFFSET_PREF, this.offset)
        editor.putInt(INIT_OFFSET_PREF, this.initOffset)
        editor.apply()
    }

    /**
     * Starts the timer if it was stopped.
     */
    @Synchronized
    fun start() {
        if (State.RUN == this.state) {
            return
        }
        updateZeroTime()
        this.state = State.RUN
    }

    private fun updateZeroTime() {
        val now = System.currentTimeMillis()
        this.zeroTime = now - this.offset.toLong() * 1000
    }

    /**
     * Stops the timer if it was run.
     */
    @Synchronized
    fun stop() {
        if (State.STOP == this.state) {
            return
        }
        this.offset = findOffset()
        this.state = State.STOP
    }

    private fun findOffset(): Int {
        val now = System.currentTimeMillis()
        return ((now - this.zeroTime) / 1000).toInt()
    }

    /**
     * Sets the initial offset of the timer.
     * @param offset   initial time offset in seconds
     */
    @Synchronized
    fun set(offset: Int) {
        this.initOffset = offset
    }

    /**
     * Resets the timer offset to the initial offset.
     * If timer is stopped, just moves the current offset.
     * If timer is run, shifts the zero time.
     */
    @Synchronized
    fun reset() {
        this.offset = this.initOffset
        if (State.RUN == this.state) {
            updateZeroTime()
        }
    }

    /**
     * Returns the current offset.
     * Negative offset means countdown till zero time. Positive offset means countup after zero time.
     * If the timer is stopped the offset doesn't change (only by #set() or #reset()).
     * If the timer is run, the offset changes according to the current time.
     */
    @get:Synchronized
    val currentOffset: Int
        get() {
            return when (this.state) {
                State.STOP -> this.offset
                State.RUN -> findOffset()
            }
        }

    /**
     * Returns true if the time is running.
     */
    @Synchronized
    fun isRunning(): Boolean {
        return State.RUN == this.state
    }
}
