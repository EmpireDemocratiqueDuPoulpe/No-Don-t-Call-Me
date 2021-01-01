package com.eddp.nodontcallme.views

import android.widget.TableLayout
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import com.eddp.nodontcallme.data.MissedCall

class DynamicTableLayout : TableLayout {
    private var ctx: Context? = null

    // Constructors
    constructor(context: Context) : super(context) { initView(context) }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initView(context) }

    private fun initView(context: Context) {
        this.ctx = context
    }

    // Data management
    fun add(missedCall: MissedCall.CallItem) {
        // Check if the row already exists
        val rowId: Int? = getRowIndexByPhoneNumber(missedCall.phoneNumber)

        if (rowId != null) {
            return update(rowId, missedCall)
        }

        // Add a row otherwise
        val newTableRow = TableRow(this.ctx)
        val phoneNumberText = TextView(ctx)
        val callsCountText = TextView(ctx)

        newTableRow.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        //phoneNumberText.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        //callsCountText.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        phoneNumberText.text = missedCall.phoneNumber
        callsCountText.text = missedCall.callsCount.toString()

        phoneNumberText.gravity = Gravity.CENTER
        callsCountText.gravity = Gravity.CENTER

        newTableRow.addView(phoneNumberText)
        newTableRow.addView(callsCountText)

        this.addView(newTableRow)
    }

    private fun getRowIndexByPhoneNumber(phoneNumber: String) : Int? {
        for (i in 0 until this.childCount) {
            val row: TableRow = this.getChildAt(i) as TableRow
            val rowPhoneNumber: String = (row.getChildAt(0) as TextView).text.toString()

            if (rowPhoneNumber == phoneNumber) {
                return i
            }
        }

        return null
    }

    fun update(rowId: Int, missedCall: MissedCall.CallItem) {
        val row: TableRow = this.getChildAt(rowId) as TableRow

        (row.getChildAt(1) as TextView).text = missedCall.callsCount.toString()
    }

    fun deleteRow(rowId: Int) {
        this.removeViewAt(rowId)
    }

    fun deleteAll() {
        this.removeAllViews()
    }
}