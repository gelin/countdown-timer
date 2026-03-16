package ru.gelin.android.countdown

import android.app.Activity
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import antistatic.spinnerwheel.AbstractWheel
import antistatic.spinnerwheel.OnWheelChangedListener
import antistatic.spinnerwheel.adapters.NumericWheelAdapter

class MainActivity : Activity(), View.OnSystemUiVisibilityChangeListener, OnWheelChangedListener {

    private lateinit var timer: Timer
    private var updater: UpdateTask? = null
    private var wheelTextSize: Float = 0f
    private var wheelsColor: Int = WHEEL_COLOR
    private val wheels = arrayOfNulls<AbstractWheel>(4)

    companion object {
        const val MAX_OFFSET = 99 * 60 + 59
        val WHEEL_TYPEFACE: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        const val WHEEL_COLOR = 0xffeeeeee.toInt()
        const val WHEEL_COLOR_RED = 0xffee0000.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.timer = Timer(this)
        setContentView(R.layout.main)

        val content = findViewById<View>(android.R.id.content)
        content.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
        content.setOnSystemUiVisibilityChangeListener(this)

        val display = windowManager.defaultDisplay
        val ratio = TypedValue()
        resources.getValue(R.dimen.wheel_text_size_ratio, ratio, true)
        this.wheelTextSize = display.height * ratio.float
        Log.d(TAG, "text size: ${this.wheelTextSize} (ratio: ${ratio.float})")

        this.wheels[0] = findViewById<View>(R.id.ten_mins) as AbstractWheel
        this.wheels[1] = findViewById<View>(R.id.mins) as AbstractWheel
        this.wheels[2] = findViewById<View>(R.id.ten_secs) as AbstractWheel
        this.wheels[3] = findViewById<View>(R.id.secs) as AbstractWheel

        initWheel(this.wheels[0], 0, 9)
        initWheel(this.wheels[1], 0, 9)
        initWheel(this.wheels[2], 0, 5)
        initWheel(this.wheels[3], 0, 9)
    }

    private fun initWheel(wheel: AbstractWheel?, min: Int, max: Int) {
        if (wheel == null) return
        val adapter = NumericWheelAdapter(this, min, max)
        adapter.textSizeUnit = TypedValue.COMPLEX_UNIT_PX
        adapter.textSize = this.wheelTextSize
        adapter.textColor = WHEEL_COLOR
        adapter.setTextTypeface(WHEEL_TYPEFACE)
        wheel.viewAdapter = adapter
        wheel.isCyclic = true
        wheel.visibleItems = 1
        wheel.addChangingListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (this.timer.isRunning()) {
            start()
        } else {
            stop()
        }
        UpdateTask(true).execute()
    }

    override fun onPause() {
        super.onPause()
        this.timer.save()
        this.updater?.stop()
    }

    override fun onSystemUiVisibilityChange(i: Int) {
        val content = findViewById<View>(android.R.id.content)
        content.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
    }

    fun start(@Suppress("UNUSED_PARAMETER") btn: View?) {
        start()
    }

    private fun start() {
        disableWheels()
        this.timer.start()
        this.updater = UpdateTask()
        this.updater?.execute()
        findViewById<View>(R.id.start_btn).visibility = View.GONE
        findViewById<View>(R.id.stop_btn).visibility = View.VISIBLE
    }

    fun stop(@Suppress("UNUSED_PARAMETER") btn: View?) {
        stop()
    }

    private fun stop() {
        this.timer.stop()
        this.updater?.stop()
        enableWheels()
        findViewById<View>(R.id.stop_btn).visibility = View.GONE
        findViewById<View>(R.id.start_btn).visibility = View.VISIBLE
    }

    fun reset(@Suppress("UNUSED_PARAMETER") btn: View?) {
        reset()
    }

    private fun reset() {
        this.timer.reset()
        updateWheels()
    }

    private fun enableWheels() {
        for (wheel in this.wheels) {
            wheel?.isEnabled = true
        }
    }

    private fun disableWheels() {
        for (wheel in this.wheels) {
            wheel?.isEnabled = false
        }
    }

    private fun updateWheels() {
        val origOffset = this.timer.currentOffset
        changeWheelsColor(if (origOffset > 0) WHEEL_COLOR_RED else WHEEL_COLOR)

        val absOffset = Math.abs(origOffset)
        val offset = if (absOffset > MAX_OFFSET) MAX_OFFSET else absOffset
        val mins = offset / 60
        val secs = offset % 60

        updateWheel(this.wheels[0], mins / 10)
        updateWheel(this.wheels[1], mins % 10)
        updateWheel(this.wheels[2], secs / 10)
        updateWheel(this.wheels[3], secs % 10)
    }

    private fun updateWheel(wheel: AbstractWheel?, value: Int) {
        wheel?.setCurrentItem(value, true, false)
    }

    private fun changeWheelsColor(color: Int) {
        if (this.wheelsColor == color) {
            return
        }
        for (wheel in this.wheels) {
            changeWheelColor(wheel, color)
        }
        this.wheelsColor = color
    }

    private fun changeWheelColor(wheel: AbstractWheel?, color: Int) {
        if (wheel == null) return
        val adapter = wheel.viewAdapter as NumericWheelAdapter
        adapter.textColor = color
        wheel.viewAdapter = adapter // to force view redraw
    }

    inner class UpdateTask(private val once: Boolean = false) : AsyncTask<Void, Void, Void>() {

        private var run = true

        fun stop() {
            this.run = false
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            if (this.once) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    // nothing to do
                }
                publishProgress()
                return null
            }
            while (this.run) {
                publishProgress()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    // nothing to do
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {
            updateWheels()
        }
    }

    override fun onChanged(changedWheel: AbstractWheel, oldValue: Int, newValue: Int) {
        if (!changedWheel.isEnabled) {
            return
        }
        if (this.timer.isRunning()) {
            return
        }
        val mins = (this.wheels[0]?.currentItem ?: 0) * 10 + (this.wheels[1]?.currentItem ?: 0)
        val secs = (this.wheels[2]?.currentItem ?: 0) * 10 + (this.wheels[3]?.currentItem ?: 0)
        this.timer.set(-(mins * 60 + secs))
        this.timer.reset()
        changeWheelsColor(WHEEL_COLOR)
    }
}
