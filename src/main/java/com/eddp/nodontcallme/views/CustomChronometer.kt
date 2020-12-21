package com.eddp.nodontcallme.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class CustomChronometer : androidx.appcompat.widget.AppCompatTextView {
    private var ctx: Context? = null

    private var startTime: Long = System.currentTimeMillis()

    private var timer: Timer? = null

    // Constructors
    constructor(context: Context) : super(context) { initView(context) }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initView(context) }

    private fun initView(context: Context) {
        this.ctx = context

        this.text = "00:00:00"
    }

    // Getters
    fun getStartTime() : Long { return this.startTime }

    // Setters
    fun setStartTime(time: Long) { this.startTime = time }

    // Functions
    fun start() {
        timer = Timer()

        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                (ctx as AppCompatActivity).runOnUiThread(ChronometerRunner())
            }

        }, 0, CHRONOMETER_INTERVAL)
    }

    private fun updateDisplay() {
        val elapsedTime: Long = System.currentTimeMillis() - startTime

        val seconds: Long = (elapsedTime / 1000) % 60
        val minutes: Long = (elapsedTime / (1000 * 60)) % 60
        val hours: Long = (elapsedTime / (1000 * 60 * 60)) % 24

        val format: String = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        this.text = format
    }

    fun stop() {
        timer?.cancel()
        timer?.purge()
        timer = null
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