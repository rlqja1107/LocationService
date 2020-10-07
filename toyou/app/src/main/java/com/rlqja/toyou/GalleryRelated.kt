package com.rlqja.toyou

import android.graphics.Bitmap
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//open class GalleryData(@PrimaryKey var id:String, var picture:Bitmap?=null):RealmObject()
//
//class GalleryRelated(private var realm: Realm) {
//    fun getPicture(id:String):GalleryData?{
//        return realm.where(GalleryData::class.java).equalTo("id",id).findFirst()
//    }
//    //지도 상에서 지도 하나만 가져올 때
//    fun getFirstPicture(id:String):Bitmap?{
//        val galleryPicture=realm.where(GalleryData::class.java).equalTo("id",id).findFirst()
//        if(galleryPicture!=null){
//            if(galleryPicture.picture!=null) {
//                return galleryPicture.picture
//            }
//            else return null
//        }
//        else return null
//    }
//}