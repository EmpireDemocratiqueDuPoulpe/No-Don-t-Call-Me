package com.eddp.nodontcallme.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val ctx: Context = context

    private val adapterCoroutine = CoroutineScope(Dispatchers.Default)
    //private var list: MutableList<MissedCall> = (
    //        listOf(MissedCall.Header) +
    //        missedCalls.map {
    //            MissedCall.CallItem(it as MissedCall.CallItem)
    //        }) as MutableList<MissedCall>

    // Add data and create views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //val v: View = LayoutInflater.from(parent.context).inflate(
        //        R.layout.recycler_view_history_row,
        //        parent,
        //        false
        //)
        //return MissedCallViewHolder(v)

        // Inflate views depending on type of item
        return when (viewType) {
            MISSED_CALL_HEADER -> MissedCallHeaderViewHolder.from(parent)
            MISSED_CALL_ITEM -> MissedCallViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    fun setData(list: MutableList<MissedCall>) {
        // Add a header to the top of the list
        this.adapterCoroutine.launch {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MissedCallViewHolder -> {
                val missedCall: MissedCall.CallItem = getItem(position) as MissedCall.CallItem

                if (position % 2 == 0) {
                    holder.view.setBackgroundColor(ctx.resources.getColor(R.color.recycler_view_even_row))
                } else {
                    holder.view.setBackgroundColor(ctx.resources.getColor(R.color.recycler_view_odd_row))
                }

                holder.bind(missedCall)
            }
        }
    }

    // Get
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MissedCall.Header -> MISSED_CALL_HEADER
            is MissedCall.CallItem -> MISSED_CALL_ITEM
        }
    }

    // Update
    //fun updateData(missedCalls: MutableList<MissedCall>) {
    //    this.list = missedCalls
    //}

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