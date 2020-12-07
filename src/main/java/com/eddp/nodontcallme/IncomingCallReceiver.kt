package com.eddp.nodontcallme

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
//import com.android.internal.telephony.ITelephony
import java.lang.reflect.Method
import android.widget.Toast
import androidx.core.app.ActivityCompat

import java.lang.Exception

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Get call info
        val state: String? = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number: String? = intent?.getExtras()?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)

        // Check if there's an incoming call
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
            // End call on Android 9+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val tm: TelecomManager = context?.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                val endCallMethod: Method = tm::class.java.getDeclaredMethod("endCall")

                endCallMethod.isAccessible = true
                endCallMethod.invoke(tm)

                Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()
                /*
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ANSWER_PHONE_CALLS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        val endedCall: Boolean = tm.endCall()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
            }
            // End call on Android 4.1->8
            else {
                val telephonyService: Any
                val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                // Try to end the call
                try {
                    val telClass: Class<*> = tm::class.java
                    val telMethod: Method = telClass.getDeclaredMethod("getITelephony")

                    telMethod.isAccessible = true
                    telephonyService = telMethod.invoke(tm) as Any

                    val telServiceClass: Class<*> = telephonyService::class.java
                    val endCallMethod: Method = telServiceClass.getDeclaredMethod("endCall")

                    endCallMethod.isAccessible = true
                    endCallMethod.invoke(telephonyService)

                    Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()

                    //val endCallMethod: Method = phoneService::class.java.getDeclaredMethod("endCall")

                    //val telephonyService: Any
                    //val telephony = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    //try {
                        //var c = Class.forName(telephony.javaClass.name)
                        //var m = c.getDeclaredMethod("getITelephony")
                        //m.isAccessible = true
                        //telephonyService = m.invoke(telephony) as Any
                        //c = Class.forName(telephonyService.javaClass.name) // Gets its class
                        //m = c.getDeclaredMethod("endCall") // Get the "endCall()' method
                        //m.isAccessible = true //Make it accessible
                        //m.invoke(telephonyService) //invoke endCall()
                    //} catch (e: Exception) {
                        //e.printStackTrace()
                    //}

                    //if (number != null) {
                        //phoneService.silenceRinger()

                        //phoneService.endCall()
                        //endCallMethod.invoke(phoneService)

                        // Show a small message to user
                        //Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()
                    //}
                } catch (e: Exception) {
                    e.printStackTrace();
                }
            }

            Toast.makeText(context, "Ring $number", Toast.LENGTH_SHORT).show()
        }

        // Off hook call
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK, ignoreCase = true)) {
            Toast.makeText(context, "Answered $number", Toast.LENGTH_SHORT).show()
        }

        // No call
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE, ignoreCase = true)) {
            Toast.makeText(context, "Idle $number", Toast.LENGTH_SHORT).show()
        }
        /*
        val phoneService: ITelephony

        try {
            val state: String? = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number: String? = intent?.getExtras()?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)

            // Check if there's an incoming call
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
                val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                // Try to end the call
                try {
                    val telMethod: Method = tm::class.java.getDeclaredMethod("getITelephony")

                    telMethod.isAccessible = true
                    phoneService = telMethod.invoke(tm) as ITelephony

                    if (number != null) {
                        phoneService.silenceRinger()
                        phoneService.endCall()

                        // Show a small message to user
                        Toast.makeText(context, "Ending the call from $number", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace();
                }

                Toast.makeText(context, "Ring $number", Toast.LENGTH_SHORT).show()
            }

            // Off hook call
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK, ignoreCase = true)) {
                Toast.makeText(context, "Answered $number", Toast.LENGTH_SHORT).show()
            }

            // No call
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE, ignoreCase = true)) {
                Toast.makeText(context, "Idle $number", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }
}


