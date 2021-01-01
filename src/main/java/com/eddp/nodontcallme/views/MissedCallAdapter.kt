package com.eddp.nodontcallme.views

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eddp.nodontcallme.R
import com.eddp.nodontcallme.data.MissedCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

class MissedCallAdapter(context: Context) :
        ListAdapter<MissedCall, RecyclerView.ViewHolder>(MissedCallDiffCallback()) {
    private val _ctx = context

    private val _adapterCoroutine = CoroutineScope(Dispatchers.Default)

    // Getters
    override fun getItemViewType(position: Int) : Int {
        return when (getItem(position)) {
            is MissedCall.Header -> MISSED_CALL_HEADER
            is MissedCall.CallItem -> MISSED_CALL_ITEM
        }
    }

    // Setters
    fun setData(list: MutableList<MissedCall>?) {
        // Add a header to the top of the list
        this._adapterCoroutine.launch {
            val completedList: List<MissedCall> = when (list) {
                null -> listOf(MissedCall.Header)
                else -> listOf(MissedCall.Header) + list.map {
                    MissedCall.CallItem(it as MissedCall.CallItem)
                }
            }

            withContext(Dispatchers.Main) {
                submitList(completedList)
            }
        }
    }

    // Create views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        // Inflate views depending on type of item
        return when (viewType) {
            MISSED_CALL_HEADER -> MissedCallHeaderViewHolder.from(parent)
            MISSED_CALL_ITEM -> MissedCallViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MissedCallViewHolder -> {
                val missedCall = getItem(position) as MissedCall.CallItem

                if (position % 2 == 0) {
                    holder.view.setBackgroundColor(ContextCompat.getColor(_ctx, R.color.recycler_view_even_row))
                } else {
                    holder.view.setBackgroundColor(ContextCompat.getColor(_ctx, R.color.recycler_view_odd_row))
                }

                holder.bind(missedCall)
            }
        }
    }

    companion object {
        private const val MISSED_CALL_HEADER = 0
        private const val MISSED_CALL_ITEM = 1
    }
}

class MissedCallDiffCallback : DiffUtil.ItemCallback<MissedCall>() {
    override fun areItemsTheSame(oldItem: MissedCall, newItem: MissedCall): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MissedCall, newItem: MissedCall): Boolean {
        return oldItem == newItem
    }
}