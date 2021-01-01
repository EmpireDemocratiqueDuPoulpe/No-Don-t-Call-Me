package com.eddp.nodontcallme

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eddp.nodontcallme.data.DatabaseHandler
import com.eddp.nodontcallme.data.MissedCall
import com.eddp.nodontcallme.interfaces.DbObserver
import com.eddp.nodontcallme.views.DynamicTableLayout
import com.eddp.nodontcallme.views.MissedCallAdapter

class HistoryFragment : Fragment() {
    //private var _context: Context? = null

    //private var database: DatabaseHandler? = null

    //private lateinit var historyTable: DynamicTableLayout
    private lateinit var historyTable: RecyclerView
    private lateinit var addRowBtn: Button

    // TODO: optimise if possible
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //database = DatabaseHandler.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_history, container, false)

        //val missedCalls: MutableList<MissedCall>? = database?.getMissedCalls()
        val adapter: MissedCallAdapter? = (activity as MainActivity).getMissedCallsAdapter()

        if (adapter != null) {
            historyTable = v.findViewById(R.id.history_recycler_view)
            historyTable.layoutManager = LinearLayoutManager(context)
            historyTable.adapter = adapter
        }

        //historyTable = v.findViewById(R.id.history_table)

        return v
    }

    // Get context
    //override fun onAttach(context: Context) {
    //    super.onAttach(context)
    //    _context = context
    //}
//
    //override fun onDetach() {
    //    super.onDetach()
    //    _context = null
    //}



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}