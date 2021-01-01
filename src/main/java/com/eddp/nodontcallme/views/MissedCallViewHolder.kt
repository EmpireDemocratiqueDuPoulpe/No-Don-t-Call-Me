package com.eddp.nodontcallme.views

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eddp.nodontcallme.R
import com.eddp.nodontcallme.data.MissedCall

// Header view holder
class MissedCallHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): MissedCallHeaderViewHolder {
            val v: View = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.recycler_view_history_header, parent, false)

            return MissedCallHeaderViewHolder(v)
        }
    }
}

// Item view holder
class MissedCallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val view: View = view
    //private val missedCallIcon: ImageView = view.findViewById(R.id.missed_call_icon)
    private val phoneNumber: TextView = view.findViewById(R.id.phone_number)
    private val callsCount: TextView = view.findViewById(R.id.calls_count)

    fun bind(missedCall: MissedCall.CallItem) {
        phoneNumber.text = missedCall.phoneNumber
        callsCount.text = missedCall.callsCount.toString()

        Log.d("PROUT", "Added:$this")
    }

    companion object {
        fun from(parent: ViewGroup): MissedCallViewHolder {
            val v: View = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.recycler_view_history_row, parent, false)

            return MissedCallViewHolder(v)
        }
    }
}