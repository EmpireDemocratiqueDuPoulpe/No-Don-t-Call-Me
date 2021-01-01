package com.eddp.nodontcallme.data

sealed class MissedCall {
    abstract val id: Long

    data class CallItem(
        var missedCallId: Int,
        var phoneNumber: String,
        var callsCount: Int
    ) : MissedCall() {
        override val id = missedCallId.toLong()

        constructor(missedCall: CallItem) :
                this(missedCall.missedCallId, missedCall.phoneNumber, missedCall.callsCount)
    }

    object Header : MissedCall() {
        override val id = Long.MIN_VALUE
    }
}