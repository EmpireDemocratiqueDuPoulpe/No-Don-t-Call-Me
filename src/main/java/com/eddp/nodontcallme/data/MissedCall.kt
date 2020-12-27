package com.eddp.nodontcallme.data

data class MissedCall (
    var missedCallId: Int,
    var phoneNumber: String,
    var callsCount: Int
)