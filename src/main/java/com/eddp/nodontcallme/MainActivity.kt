package com.eddp.nodontcallme

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.eddp.nodontcallme.data.DatabaseHandler
import com.eddp.nodontcallme.data.MissedCall
import com.eddp.nodontcallme.interfaces.DbObserver
import com.eddp.nodontcallme.views.MissedCallAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), DbObserver {
    private var _database: DatabaseHandler? = null
    private var _missedCalls: MutableList<MissedCall>? = null
    private var _missedCallAdapter: MissedCallAdapter? = null

    private var _serviceIntent: Intent? = null
    private var _callBlockerService: CallBlockerService? = null
    private var _callBlockerDataReceiver: CallBlockerDataReceiver? = null

    private lateinit var _viewPager: ViewPager2
    private lateinit var _viewPagerAdapter: ViewPagerAdapter
    private lateinit var _bottomNavBar: BottomNavBar

    // Getters
    fun getMissedCalls() : MutableList<MissedCall>? = this._missedCalls
    fun getMissedCallsAdapter() : MissedCallAdapter? = this._missedCallAdapter

    fun getServiceIntent() : Intent? = this._serviceIntent
    fun getCallBlockerService() : CallBlockerService? = this._callBlockerService
    fun getCallBlockerDataReceiver() : CallBlockerDataReceiver? = this._callBlockerDataReceiver

    fun getViewPager() : ViewPager2 = this._viewPager

    // Overridden functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask for permission (Android 6.0+)
        // TODO: Check if needed or for better way to do it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) {
                val permissions: Array<String> = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS)

                requestPermissions(permissions, PERMISSION_REQUEST_READ_PHONE_STATE)
            }
        }

        // Get database and data
        this._database = DatabaseHandler.getInstance(this)
        this._database?.registerObserver(this)
        this._missedCalls = this._database?.getMissedCalls() as MutableList<MissedCall>
        this._missedCallAdapter = MissedCallAdapter(this)

        if (this._missedCalls != null && this._missedCallAdapter != null) {
            this._missedCallAdapter!!.setData(this._missedCalls!!)
        }

        // Init view
        this._bottomNavBar = BottomNavBar()

        this._viewPager = findViewById(R.id.pager)
        this._viewPagerAdapter = ViewPagerAdapter(this)
        this._viewPager.adapter = this._viewPagerAdapter
        this._viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                _bottomNavBar.setSelectedItem(position)
            }
        })

        // Get service
        this._callBlockerService = CallBlockerService()
        this._callBlockerDataReceiver = CallBlockerDataReceiver()
        val intentFilter = IntentFilter(DATA_RECEIVER_ACTION_CHRONOMETER_DATA)

        registerReceiver(this._callBlockerDataReceiver, intentFilter)

        if (this._callBlockerService != null) {
            this._serviceIntent = Intent(this, this._callBlockerService!!::class.java)
        }
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

    override fun onBackPressed() {
        if (this._viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            this._viewPager.currentItem = this._viewPager.currentItem - 1
        }
    }

    override fun onDestroy() {
        //val broadcastIntent = Intent()
        //broadcastIntent.action = "restartservice"
        //broadcastIntent.setClass(this, Restarter::class.java)
        //this.sendBroadcast(broadcastIntent)

        super.onDestroy()
    }

    // Database
    override fun onDatabaseChanged() {
        this._missedCalls = this._database?.getMissedCalls() as MutableList<MissedCall>

        if (_missedCalls != null) {
            this._missedCallAdapter?.setData(this._missedCalls!!)
        }
    }

    // UI / Navigation
    inner class BottomNavBar : BottomNavigationView.OnNavigationItemSelectedListener {
        private var _bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        private val _items: MutableList<MenuItem> = ArrayList()
        private var _previousSelectedMenu: Int = 0

        init {
            // Get menu items
            val menu = this._bottomNavBar.menu

            for (i in 0 until menu.size()) {
                this._items.add(menu.getItem(i))
            }

            // Add events
            this._bottomNavBar.setOnNavigationItemSelectedListener(this)
        }

        // Getters
        fun getCurrentItem() : Int = this._bottomNavBar.selectedItemId

        // Setters
        fun setSelectedItem(position: Int) {
            this._previousSelectedMenu = this._bottomNavBar.selectedItemId
            this._items[position].isChecked = true
        }

        // Navigation
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            // Get current item pos
            val itemPos = this._items.indexOf(item)
            if (this._previousSelectedMenu == itemPos) return false

            // Update
            getViewPager().setCurrentItem(itemPos, true)
            this._previousSelectedMenu = itemPos

            return true
        }
    }

    inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        private val _fragments: List<Fragment> = listOf(
                CallBlockerFragment(),
                HistoryFragment(),
                SettingsFragment()
        )

        fun getFragmentById(position: Int) : Fragment = this._fragments[position]

        override fun getItemCount() : Int = this._fragments.size

        override fun createFragment(position: Int) : Fragment = this._fragments[position]
    }

    // Service
    // TODO: See https://stackoverflow.com/questions/45817813/alternate-of-activitymanager-getrunningservicesint-after-oreo
    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }

    inner class CallBlockerDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DATA_RECEIVER_ACTION_CHRONOMETER_DATA -> {
                    val chronometerStartTime: Long = getChronometerStartTime() ?: return

                    val currentFragment: Fragment? =
                            _viewPagerAdapter.getFragmentById(FRAGMENT_ID_CALL_BLOCKER)

                    if (currentFragment != null) {
                        if (currentFragment is CallBlockerFragment) {
                            if (currentFragment.isVisible) {
                                currentFragment.showChronometer(chronometerStartTime)
                            }
                        }
                    }
                }
            }
        }

        fun getChronometerStartTime() : Long? {
            val sharedPrefs: SharedPreferences? = this@MainActivity.getSharedPreferences(
                    getString(R.string.shared_pref_filename),
                    MODE_PRIVATE
            )
            return sharedPrefs?.getLong("start_time", 0)
        }
    }

    companion object {
        const val PERMISSION_REQUEST_READ_PHONE_STATE = 0

        const val FRAGMENT_ID_CALL_BLOCKER = 0
        const val FRAGMENT_ID_HISTORY = 0
        const val FRAGMENT_ID_SETTINGS = 0

        const val DATA_RECEIVER_ACTION_CHRONOMETER_DATA = "CHRONOMETER_DATA"
    }
}