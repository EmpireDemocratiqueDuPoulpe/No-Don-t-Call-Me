package com.eddp.nodontcallme

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.eddp.nodontcallme.views.AnimatedHowToUse
import com.eddp.nodontcallme.views.CustomChronometer
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private var serviceIntent: Intent? = null
    private var callBlockerService: CallBlockerService? = null
    private var callBlockerDataReceiver: CallBlockerDataReceiver? = null

    private lateinit var bottomNavBar: BottomNavigationView
    private val bottomNavBarItems: MutableList<MenuItem> = ArrayList()
    private var previousSelectedMenu: Int = 0

    private lateinit var fragmentManager: FragmentManager

    //private lateinit var howToUse: AnimatedHowToUse
    //private lateinit var startBlockerBtn: Button
    //private lateinit var chronometer: CustomChronometer

    // Getters
    fun getServiceIntent() : Intent? { return this.serviceIntent }
    fun getCallBlockerService() : CallBlockerService? { return this.callBlockerService }
    fun getCallBlockerDataReceiver() : CallBlockerDataReceiver? { return this.callBlockerDataReceiver }

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

        // Init view
        fragmentManager = supportFragmentManager

        fragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, CallBlockerFragment(), FRAGMENT_CALL_BLOCKER)
            .addToBackStack(null)
            .commit()

        // Get elements
        bottomNavBar = findViewById(R.id.bottom_nav_bar)
        val menu = bottomNavBar.menu

        for (i in 0 until menu.size()) {
            bottomNavBarItems.add(menu.getItem(i))
        }

        bottomNavBar.setOnNavigationItemSelectedListener(BottomNavBarListener())
        //howToUse = findViewById(R.id.how_to_use)
        //howToUse.setView(howToUse)
        //startBlockerBtn = findViewById(R.id.btn_start_blocker)
        //chronometer = findViewById(R.id.chronometer)

        // Get service
        callBlockerService = CallBlockerService()
        callBlockerDataReceiver = CallBlockerDataReceiver()
        val intentFilter = IntentFilter(DATA_RECEIVER_ACTION_CHRONOMETER_DATA)

        registerReceiver(callBlockerDataReceiver, intentFilter)

        if (callBlockerService != null) {
            serviceIntent = Intent(this, callBlockerService!!::class.java)

            //if (isServiceRunning(callBlockerService!!::class.java)) {
            //    startBlockerBtn.text = CallBlockerBtnListener().enabledText
            //    howToUse.toggle(false)
            //    showChronometer(callBlockerDataReceiver?.getChronometerStartTime() ?: 0)
            //} else {
            //    startBlockerBtn.text = CallBlockerBtnListener().disabledText
            //    howToUse.toggle(true)
            //    hideChronometer()
            //}
//
            //startBlockerBtn.setOnClickListener(CallBlockerBtnListener())
        }
    }

    //fun showChronometer(time: Long) {
    //    chronometer.setStartTime(time)
    //    chronometer.start()
//
    //    chronometer.animate().alpha(1f).setDuration(200)
    //}
//
    //fun hideChronometer() {
    //    chronometer.stop()
//
    //    chronometer.animate().alpha(0f).setDuration(200)
    //}

    // TODO("See https://stackoverflow.com/questions/45817813/alternate-of-activitymanager-getrunningservicesint-after-oreo")
    fun isServiceRunning(serviceClass: Class<*>): Boolean {
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

        //val broadcastIntent = Intent()
        //broadcastIntent.action = "restartservice"
        //broadcastIntent.setClass(this, Restarter::class.java)
        //this.sendBroadcast(broadcastIntent)

        super.onDestroy()
    }

    //inner class CallBlockerBtnListener : View.OnClickListener {
    //    val disabledText: String = getString(R.string.btn_blocker_stopped)
    //    val enabledText: String = getString(R.string.btn_blocker_started)
//
    //    override fun onClick(v: View?) {
    //        if (v == null) return
    //        if (v.id != R.id.btn_start_blocker) return
//
    //        if (isServiceRunning(callBlockerService!!::class.java)) {
    //            stopService(serviceIntent)
    //            startBlockerBtn.text = disabledText
    //            howToUse.toggle(true)
    //            hideChronometer()
    //        } else {
    //            startService(serviceIntent)
    //            startBlockerBtn.text = enabledText
    //            howToUse.toggle(false)
    //        }
    //    }
    //}

    inner class CallBlockerDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                DATA_RECEIVER_ACTION_CHRONOMETER_DATA -> {
                    val chronometerStartTime: Long = getChronometerStartTime() ?: return

                    val currentFragment: CallBlockerFragment? = fragmentManager.findFragmentByTag(FRAGMENT_CALL_BLOCKER) as CallBlockerFragment

                    if (currentFragment != null && currentFragment.isVisible) {
                        currentFragment.showChronometer(chronometerStartTime)
                    }
                }
            }
        }

        fun getChronometerStartTime() : Long? {
            val sharedPrefs: SharedPreferences? = this@MainActivity.getSharedPreferences(getString(R.string.shared_pref_filename), MODE_PRIVATE)
            return sharedPrefs?.getLong("start_time", 0)
        }
    }

    inner class BottomNavBarListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            // Get current item pos
            val itemPos = bottomNavBarItems.indexOf(item)
            if (previousSelectedMenu == itemPos) return false

            // Init transaction vars
            var fragment: Fragment
            var fragmentTag: String
            var enterAnim: Int
            var exitAnim: Int

            when (item.itemId) {
                R.id.menu_page_call_blocker -> {
                    fragment = CallBlockerFragment()
                    fragmentTag = FRAGMENT_CALL_BLOCKER
                }
                R.id.menu_page_history -> {
                    fragment = HistoryFragment()
                    fragmentTag = FRAGMENT_HISTORY
                }
                R.id.menu_page_settings -> {
                    fragment = SettingsFragment()
                    fragmentTag = FRAGMENT_SETTINGS
                }
                else -> {
                    return false
                }
            }

            if (previousSelectedMenu > itemPos) {
                enterAnim = R.anim.slide_in_left
                exitAnim = R.anim.slide_out_right
            } else {
                enterAnim = R.anim.slide_in_right
                exitAnim = R.anim.slide_out_left
            }

            // Begin transaction
            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            enterAnim,
                            exitAnim,
                            enterAnim,
                            exitAnim)
                    .replace(R.id.nav_host_fragment, fragment, fragmentTag)
                    .addToBackStack(null)
                    .commit()

            // Update previous selected item
            previousSelectedMenu = itemPos

            return true
        }
    }

    companion object {
        const val PERMISSION_REQUEST_READ_PHONE_STATE = 0

        const val FRAGMENT_CALL_BLOCKER = "CALL_BLOCKER_FRAGMENT"
        const val FRAGMENT_HISTORY = "HISTORY_FRAGMENT"
        const val FRAGMENT_SETTINGS = "SETTING_FRAGMENT"

        const val DATA_RECEIVER_ACTION_CHRONOMETER_DATA = "CHRONOMETER_DATA"
    }
}