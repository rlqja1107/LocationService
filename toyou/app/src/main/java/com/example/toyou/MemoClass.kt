package com.example.toyou

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import java.util.*

open class MemoData(@PrimaryKey var id:String= UUID.randomUUID().toString(), var latitude:Double=0.0, var longitude:Double=0.0, var contents:String="", var title:String="",
                    var year:Int=0, var month:Int=0, var day:Int=0, var hour:Int=0, var minute:Int=0):
    RealmObject()

class MemoClass(private val realm: Realm) {
    fun getAllMemo(): RealmResults<MemoData>{
        return realm.where(MemoData::class.java)
            .findAll()
    }
}