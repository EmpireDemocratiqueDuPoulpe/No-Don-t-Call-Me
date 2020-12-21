package com.eddp.nodontcallme

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.reflect.Method
import java.util.*


/******************************************
* # Call Blocker service
*
* Made with help of:
* https://stackoverflow.com/questions/30525784/android-keep-service-running-when-app-is-killed
 ******************************************/

class CallBlockerService : Service() {
    private val _binder: IBinder = ServiceBinder()

    private var broadcastReceiver: BroadcastReceiver? = null

    private var startTime: Long? = null

    // Getters
    // TODO("Move MainActivity.CallBlockerDataReceiver.getChronometerStartTime() here?")
    fun getStartTime() : Long { return this.startTime ?: -1 }

    // On create
    override fun onCreate() {
        super.onCreate()

        // Start service and notification system
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startOwnForeground()
        } else {
            startTime = System.currentTimeMillis()
            startForeground(1, Notification())
        }

        startCallBlocker()
    }

    // Android Oreo (8 - SDK 27) need notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startOwnForeground() {
        // Notification channel
        val notifChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        )

        notifChannel.lightColor = Color.RED
        notifChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        // Notification manager
        val notifManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(notifChannel)

        // Notification builder
        val notifBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID
        )

        // Notification
        startTime = System.currentTimeMillis()
        saveStartTime()

        val notif: Notification = notifBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Call Blocker is active")
                .setWhen(startTime!!)
                .setUsesChronometer(true)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

        startForeground(2, notif)
    }

    private fun startCallBlocker() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PHONE_STATE")

        broadcastReceiver = CallBlocker()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun saveStartTime() {
        val sharedPref = getSharedPreferences(getString(R.string.shared_pref_filename), MODE_PRIVATE) ?: return

        with(sharedPref.edit()) {
            startTime?.let { putLong("start_time", it) }
            commit()
        }

        // Send signal
        val chronometerBroadcast = Intent(MainActivity.DATA_RECEIVER_ACTION_CHRONOMETER_DATA)
        this.sendBroadcast(chronometerBroadcast)
    }

    // On start command / On bind
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startCallBlocker()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return _binder
    }

    inner class ServiceBinder : Binder() {
        fun getService(): CallBlockerService {
            return this@CallBlockerService
        }
    }

    // On destroy
    // TODO("Leaked broadcast receiver error each time the service is stopped")
    override fun onDestroy() {
        stopCallBlocker()
        super.onDestroy()

        //val restartBroadcast = Intent()
        //restartBroadcast.action = "restartservice"
        //restartBroadcast.setClass(this, Restarter::class.java)
        //this.sendBroadcast(restartBroadcast)
    }


    private fun stopCallBlocker() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
            startTime = null
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID: String = "eddp.nodontcallme.callblocker"
        const val NOTIFICATION_CHANNEL_NAME: String = "Call Blocker Service"
    }


    inner class CallBlocker : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Get call info
            val state: String? = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number: String? = intent?.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)

            // Check if there's an incoming call
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
                // End call on Android 9+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    blockCallAndroid9Plus(context, number)
                }
                // End call on Android 4.1->8
                else {
                    blockCallAndroid4Plus(context, number)
                }
            }

            // Off hook call
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK, ignoreCase = true)) {
                Toast.makeText(context, "Answered $number", Toast.LENGTH_SHORT).show()
            }

            // No call
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE, ignoreCase = true)) {
                Toast.makeText(context, "Idle $number", Toast.LENGTH_SHORT).show()
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        private fun blockCallAndroid9Plus(context: Context?, number: String?) {
            val tm: TelecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val endCallMethod: Method = tm::class.java.getDeclaredMethod("endCall")

            endCallMethod.isAccessible = true
            endCallMethod.invoke(tm)

            Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()
        }

        private fun blockCallAndroid4Plus(context: Context?, number: String?) {
            val telephonyService: Any
            val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Try to end the call
            try {
                // Get ITelephony service
                val telClass: Class<*> = tm::class.java
                val telMethod: Method = telClass.getDeclaredMethod("getITelephony")

                telMethod.isAccessible = true
                telephonyService = telMethod.invoke(tm) as Any

                // Get endCall() method
                val telServiceClass: Class<*> = telephonyService::class.java
                val endCallMethod: Method = telServiceClass.getDeclaredMethod("endCall")

                endCallMethod.isAccessible = true
                endCallMethod.invoke(telephonyService)

                Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}