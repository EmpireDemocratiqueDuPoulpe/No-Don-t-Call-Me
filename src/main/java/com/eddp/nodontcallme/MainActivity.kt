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
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eddp.nodontcallme.views.AnimatedHowToUse

class MainActivity : AppCompatActivity() {
    private var serviceIntent: Intent? = null
    private var callBlockerService: CallBlockerService? = null

    private lateinit var howToUse: AnimatedHowToUse
    private lateinit var startBlockerBtn: Button

    //private var shortAnimationDuration: Int = 0
    //private lateinit var howToUseAnimShow: TranslateAnimation
    //private lateinit var howToUseAnimHide: TranslateAnimation

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

        // Get service
        callBlockerService = CallBlockerService()

        if (callBlockerService != null) {
            serviceIntent = Intent(this, callBlockerService!!::class.java)

            if (isServiceRunning(callBlockerService!!::class.java)) {
                startBlockerBtn.text = CallBlockerBtnListener().enabledText
                howToUse.toggle(false)
            } else {
                startBlockerBtn.text = CallBlockerBtnListener().disabledText
                howToUse.toggle(true)
            }

            startBlockerBtn.setOnClickListener(CallBlockerBtnListener())
        }

        // Init anims
        //shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        //howToUseAnimShow = TranslateAnimation(0f, 0f, 0f, -howToUse.height.toFloat())
        //howToUseAnimHide = TranslateAnimation(0f, 0f, -howToUse.height.toFloat(), 0f)
//
        //howToUseAnimShow.duration = shortAnimationDuration.toLong()
        //howToUseAnimHide.duration = shortAnimationDuration.toLong()
//
        //howToUseAnimShow.interpolator = AccelerateInterpolator()
        //howToUseAnimHide.interpolator = AccelerateInterpolator()
//
        //howToUseAnimShow.setAnimationListener(HowToUseAnimListener(true))
        //howToUseAnimHide.setAnimationListener(HowToUseAnimListener(false))
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

    //private fun startBlocker() {
    //    howToUse.animate()
    //            .alpha(0f)
    //            .y(-500f)
    //            .setDuration(shortAnimationDuration.toLong())
    //            .setListener(object: AnimatorListenerAdapter() {
    //                override fun onAnimationEnd(animation: Animator?) {
    //                    howToUse.visibility = View.GONE
    //                }
    //            })
//
    //    //startBlockerBtn.setText(sBBTextOn)
    //}

    //private fun endBlocker() {
    //    howToUse.animate()
    //            .alpha(1f)
    //            .y(0f)
    //            .setDuration(shortAnimationDuration.toLong())
    //            .setListener(object: AnimatorListenerAdapter() {
    //                override fun onAnimationStart(animation: Animator?) {
    //                    howToUse.visibility = View.VISIBLE
    //                }
    //            })
//
    //    //startBlockerBtn.setText(sBBTextOff)
    //}

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
            } else {
                startService(serviceIntent)
                startBlockerBtn.text = enabledText
                howToUse.toggle(false)
            }
        }
    }

    //inner class HowToUseAnimListener(show: Boolean) : Animation.AnimationListener {
    //    private val showView: Boolean = show
//
    //    override fun onAnimationStart(animation: Animation?) {
    //        if (showView) {
//
    //        }
    //    }
//
    //    override fun onAnimationEnd(animation: Animation?) { }
//
    //    override fun onAnimationRepeat(animation: Animation?) { }
//
    //}

    companion object {
        const val PERMISSION_REQUEST_READ_PHONE_STATE = 0
    }
}