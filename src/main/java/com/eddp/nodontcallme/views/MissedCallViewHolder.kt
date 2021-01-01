package com.eddp.nodontcallme.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class MissedCallViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val _phoneNumber: TextView = view.findViewById(R.id.phone_number)
    private val _callsCount: TextView = view.findViewById(R.id.calls_count)

    fun bind(missedCall: MissedCall.CallItem) {
        this._phoneNumber.text = missedCall.phoneNumber
        this._callsCount.text = missedCall.callsCount.toString()
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