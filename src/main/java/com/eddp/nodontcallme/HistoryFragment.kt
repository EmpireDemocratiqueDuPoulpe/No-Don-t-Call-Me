package com.eddp.nodontcallme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eddp.nodontcallme.views.MissedCallAdapter

class HistoryFragment : Fragment() {
    private lateinit var _historyTable: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_history, container, false)

        val adapter: MissedCallAdapter? = (activity as MainActivity).getMissedCallsAdapter()

        if (adapter != null) {
            this._historyTable = v.findViewById(R.id.history_recycler_view)
            this._historyTable.layoutManager = LinearLayoutManager(context)
            this._historyTable.adapter = adapter
        }

        return v
    }

    companion object {
        //@JvmStatic
        //fun newInstance(param1: String, param2: String) =
        //    HistoryFragment().apply {
        //        arguments = Bundle().apply {
        //        }
        //    }
    }
}