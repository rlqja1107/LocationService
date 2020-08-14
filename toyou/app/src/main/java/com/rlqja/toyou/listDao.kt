package com.rlqja.toyou

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class listDao(private val realm: Realm) {
    fun getAllDemo():RealmResults<listData>{
        return realm.where(listData::class.java)
            .sort("Time", Sort.DESCENDING)
            .findAll()
    }

}