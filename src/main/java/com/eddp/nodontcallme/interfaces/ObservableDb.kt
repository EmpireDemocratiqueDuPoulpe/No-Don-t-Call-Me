package com.eddp.nodontcallme.interfaces

interface ObservableDb {
    fun registerObserver(dbObserver: DbObserver)
    fun removeObserver(dbObserver: DbObserver)
    fun notifyChange()
}