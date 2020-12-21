package com.eddp.nodontcallme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.eddp.nodontcallme.views.AnimatedHowToUse
import com.eddp.nodontcallme.views.CustomChronometer

class CallBlockerFragment : Fragment() {

    private var _activity: MainActivity? = null

    private lateinit var howToUse: AnimatedHowToUse
    private lateinit var startBlockerBtn: Button
    private lateinit var chronometer: CustomChronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_call_blocker, container, false)

        // Get elements
        howToUse = v.findViewById(R.id.how_to_use)
        howToUse.setView(howToUse)
        startBlockerBtn = v.findViewById(R.id.btn_start_blocker)
        chronometer = v.findViewById(R.id.chronometer)

        // Update view according to service status
        if (_activity?.isServiceRunning(_activity?.getCallBlockerService()!!::class.java) == true) {
            startBlockerBtn.text = CallBlockerBtnListener().enabledText
            howToUse.toggle(false)
            showChronometer(_activity?.getCallBlockerDataReceiver()?.getChronometerStartTime() ?: 0)
        } else {
            startBlockerBtn.text = CallBlockerBtnListener().disabledText
            howToUse.toggle(true)
            hideChronometer()
        }

        startBlockerBtn.setOnClickListener(CallBlockerBtnListener())

        return v
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CallBlockerFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                }
            }
    }

    inner class CallBlockerBtnListener : View.OnClickListener {
        val disabledText: String = getString(R.string.btn_blocker_stopped)
        val enabledText: String = getString(R.string.btn_blocker_started)

        override fun onClick(v: View?) {
            if (v == null) return
            if (v.id != R.id.btn_start_blocker) return

            if (_activity?.isServiceRunning(_activity?.getCallBlockerService()!!::class.java) == true) {
                _activity?.stopService(_activity?.getServiceIntent())
                startBlockerBtn.text = disabledText
                howToUse.toggle(true)
                hideChronometer()
            } else {
                _activity?.startService(_activity?.getServiceIntent())
                startBlockerBtn.text = enabledText
                howToUse.toggle(false)
            }
        }
    }
}