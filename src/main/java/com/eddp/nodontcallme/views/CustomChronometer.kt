package com.eddp.nodontcallme.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import com.eddp.nodontcallme.R
import java.util.*

class CustomChronometer : androidx.appcompat.widget.AppCompatTextView {
    private lateinit var _ctx: Context

    private var _startTime: Long = System.currentTimeMillis()
    private var _timer: Timer? = null

    // Constructors
    constructor(context: Context) : super(context) { initView(context) }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initView(context) }

    private fun initView(context: Context) {
        this._ctx = context

        this.text = this._ctx.getString(R.string.default_chronometer_text)
    }

    // Getters
    fun getStartTime() : Long { return this._startTime }

    // Setters
    fun setStartTime(time: Long) { this._startTime = time }

    // Functions
    fun start() {
        _timer = Timer()

        _timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (_ctx as AppCompatActivity).runOnUiThread(ChronometerRunner())
            }

        }, 0, CHRONOMETER_INTERVAL)
    }

    private fun updateDisplay() {
        val elapsedTime: Long = System.currentTimeMillis() - this._startTime

        val seconds: Long = (elapsedTime / 1000) % 60
        val minutes: Long = (elapsedTime / (1000 * 60)) % 60
        val hours: Long = (elapsedTime / (1000 * 60 * 60)) % 24

        val format = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        this.text = format
    }

    fun stop() {
        this._timer?.cancel()
        this._timer?.purge()
        this._timer = null
    }

    companion object {
        const val CHRONOMETER_INTERVAL: Long = 1000
    }

    // Runner
    inner class ChronometerRunner : Runnable {
        override fun run() {
            updateDisplay()
        }
    }
}