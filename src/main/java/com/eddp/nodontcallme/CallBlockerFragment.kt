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

    private lateinit var _howToUse: AnimatedHowToUse
    private lateinit var _startBlockerBtn: Button
    private lateinit var _chronometer: CustomChronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this._activity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_call_blocker, container, false)

        // Get elements
        this._howToUse = v.findViewById(R.id.how_to_use)
        this._howToUse.setView(this._howToUse)
        this._startBlockerBtn = v.findViewById(R.id.btn_start_blocker)
        this._chronometer = v.findViewById(R.id.chronometer)

        // Update view according to service status
        if (_activity?.isServiceRunning(_activity?.getCallBlockerService()!!::class.java) == true) {
            this._startBlockerBtn.text = CallBlockerBtnListener().enabledText
            this._howToUse.toggle(false)
            showChronometer(_activity?.getCallBlockerDataReceiver()?.getChronometerStartTime() ?: 0)
        } else {
            this._startBlockerBtn.text = CallBlockerBtnListener().disabledText
            this._howToUse.toggle(true)
            hideChronometer()
        }

        this._startBlockerBtn.setOnClickListener(CallBlockerBtnListener())

        return v
    }

    fun showChronometer(time: Long) {
        this._chronometer.setStartTime(time)
        this._chronometer.start()

        this._chronometer.animate().alpha(1f).duration = 200
    }

    fun hideChronometer() {
        this._chronometer.stop()

        this._chronometer.animate().alpha(0f).duration = 200
    }

    inner class CallBlockerBtnListener : View.OnClickListener {
        val disabledText: String = getString(R.string.btn_blocker_stopped)
        val enabledText: String = getString(R.string.btn_blocker_started)

        override fun onClick(v: View?) {
            if (v == null) return
            if (v.id != R.id.btn_start_blocker) return

            if (_activity?.isServiceRunning(_activity?.getCallBlockerService()!!::class.java) == true) {
                _activity?.stopService(_activity?.getServiceIntent())
                _startBlockerBtn.text = disabledText
                _howToUse.toggle(true)
                hideChronometer()
            } else {
                _activity?.startService(_activity?.getServiceIntent())
                _startBlockerBtn.text = enabledText
                _howToUse.toggle(false)
            }
        }
    }

    companion object {
        //@JvmStatic
        //fun newInstance(param1: String, param2: String) =
        //    CallBlockerFragment().apply {
        //        arguments = Bundle().apply {}
        //    }
    }
}