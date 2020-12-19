package com.eddp.nodontcallme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

class Restarter : BroadcastReceiver() {
    // TODO("REMOVE ?")
    override fun onReceive(context: Context?, intent: Intent?) {
        //Toast.makeText(context, "Restarting Call Blocker service...", Toast.LENGTH_SHORT).show()
//
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    context?.startForegroundService(Intent(context, CallBlockerService::class.java))
        //} else {
        //    context?.startService(Intent(context, CallBlockerService::class.java))
        //}
    }
}