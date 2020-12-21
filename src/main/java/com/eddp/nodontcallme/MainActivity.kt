package com.eddp.nodontcallme

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eddp.nodontcallme.views.AnimatedHowToUse
import com.eddp.nodontcallme.views.CustomChronometer


class MainActivity : AppCompatActivity() {
    private var serviceIntent: Intent? = null
    private var callBlockerService: CallBlockerService? = null
    private var callBlockerDataReceiver: CallBlockerDataReceiver? = null

    private lateinit var howToUse: AnimatedHowToUse
    private lateinit var startBlockerBtn: Button
    private lateinit var chronometer: CustomChronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask for permission (Android 6.0+)
        // TODO("CHECK IF NEEDED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) {
                val permissions: Array<String> = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS)

                requestPermissions(permissions, PERMISSION_REQUEST_READ_PHONE_STATE)
            }
        }

        // Get elements
        howToUse = findViewById(R.id.how_to_use)
        howToUse.setView(howToUse)
        startBlockerBtn = findViewById(R.id.btn_start_blocker)
        chronometer = findViewById(R.id.chronometer)

        // Get service
        callBlockerService = CallBlockerService()
        callBlockerDataReceiver = CallBlockerDataReceiver()
        val intentFilter = IntentFilter(DATA_RECEIVER_ACTION_CHRONOMETER_DATA)

        registerReceiver(callBlockerDataReceiver, intentFilter)

        if (callBlockerService != null) {
            serviceIntent = Intent(this, callBlockerService!!::class.java)

            if (isServiceRunning(callBlockerService!!::class.java)) {
                startBlockerBtn.text = CallBlockerBtnListener().enabledText
                howToUse.toggle(false)
                showChronometer(callBlockerDataReceiver?.getChronometerStartTime() ?: 0)
            } else {
                startBlockerBtn.text = CallBlockerBtnListener().disabledText
                howToUse.toggle(true)
                hideChronometer()
            }

            startBlockerBtn.setOnClickListener(CallBlockerBtnListener())
        }
    }

    fun showChronometer(time: Long) {
        chronometer.setStartTime(time)
        chronometer.start()

        chronometer.animate().alpha(1f).setDuration(200)
    }

    fun hideChronometer() {
        chronometer.stop()

        chronometer.animate().alpha(0f).setDuration(200)
    }

    // TODO("See https://stackoverflow.com/questions/45817813/alternate-of-activitymanager-getrunningservicesint-after-oreo")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted: $PERMISSION_REQUEST_READ_PHONE_STATE", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission not granted: $PERMISSION_REQUEST_READ_PHONE_STATE", Toast.LENGTH_SHORT).show()
                }

                return
            }
        }
    }

    override fun onDestroy() {
        //stopService(mServiceIntent);

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, Restarter::class.java)
        this.sendBroadcast(broadcastIntent)

        super.onDestroy()
    }

    inner class CallBlockerBtnListener : View.OnClickListener {
        val disabledText: String = getString(R.string.btn_blocker_stopped)
        val enabledText: String = getString(R.string.btn_blocker_started)

        override fun onClick(v: View?) {
            if (v == null) return
            if (v.id != R.id.btn_start_blocker) return

            if (isServiceRunning(callBlockerService!!::class.java)) {
                stopService(serviceIntent)
                startBlockerBtn.text = disabledText
                howToUse.toggle(true)
                hideChronometer()
            } else {
                startService(serviceIntent)
                startBlockerBtn.text = enabledText
                howToUse.toggle(false)
            }
        }
    }

    inner class CallBlockerDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //val str = intent?.getExtras()?.get("Extra data name").toString() as String

            when (intent?.action) {
                DATA_RECEIVER_ACTION_CHRONOMETER_DATA -> {
                    //val chronometerStartTime: Long = intent.extras?.get("start_time") as Long
                    //Log.d("PROUT", chronometerStartTime.toString())
                    //MainActivity  .showChronometer(callBlockerService!!.getStartTime())

                    val chronometerStartTime: Long = getChronometerStartTime() ?: return

                    showChronometer(chronometerStartTime)
                }
            }
        }

        fun getChronometerStartTime() : Long? {
            val sharedPrefs: SharedPreferences? = this@MainActivity.getSharedPreferences(getString(R.string.shared_pref_filename), MODE_PRIVATE)
            return sharedPrefs?.getLong("start_time", 0)
        }
    }

    companion object {
        const val PERMISSION_REQUEST_READ_PHONE_STATE = 0

        const val DATA_RECEIVER_ACTION_CHRONOMETER_DATA = "CHRONOMETER_DATA"
    }
}