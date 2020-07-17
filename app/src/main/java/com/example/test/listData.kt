package com.example.test

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
//데이터베이스에 저장할 속성지정
data class KickBoardData(var latitude: Double,var longitude: Double,var kinds:Int,var battery:Int)
open class listData(
    //고유키
    @PrimaryKey
    var id:String=UUID.randomUUID().toString(),
    var location:String="",
    var address:String="",
    var longitude:Double=0.0,
    var latitude:Double=0.0,
    var Time:Date=Date()
):RealmObject()