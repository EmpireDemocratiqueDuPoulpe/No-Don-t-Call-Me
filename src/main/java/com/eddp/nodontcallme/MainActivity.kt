package com.eddp.nodontcallme

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val PERMISSION_REQUEST_READ_PHONE_STATE = 0
    //private val BLOCKER_RUNNING = 0
    //private val BLOCKER_STOPPED = 1

    private var serviceIntent: Intent? = null
    private var callBlockerService: CallBlockerService? = null

    private lateinit var howToUse: RelativeLayout
    private lateinit var startBlockerBtn: Button

    private var shortAnimationDuration: Int = 0
    private lateinit var sBBTextOn: String
    private lateinit var sBBTextOff: String

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

        // Get service
        callBlockerService = CallBlockerService()

        if (callBlockerService != null) {
            serviceIntent = Intent(this, callBlockerService!!::class.java)

            if (!isServiceRunning(callBlockerService!!::class.java)) {
                startService(serviceIntent)
            }
        }

        // Get elements
        howToUse = findViewById(R.id.how_to_use)
        startBlockerBtn = findViewById(R.id.btn_start_blocker)

        // Init anims
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        sBBTextOn = resources.getString(R.string.btn_end_blocker)
        sBBTextOff = resources.getString(R.string.btn_start_blocker)

        // Add events listeners
        startBlockerBtn.setOnClickListener(this)
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

    override fun onClick(view: View) {
        when (view.getId()) {
            R.id.btn_start_blocker -> {
                // CODE
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

    private fun startBlocker() {
        howToUse.animate()
                .alpha(0f)
                .y(-500f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        howToUse.visibility = View.GONE
                    }
                })

        startBlockerBtn.setText(sBBTextOn)
    }

    private fun endBlocker() {
        howToUse.animate()
                .alpha(1f)
                .y(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        howToUse.visibility = View.VISIBLE
                    }
                })

        startBlockerBtn.setText(sBBTextOff)
    }
}