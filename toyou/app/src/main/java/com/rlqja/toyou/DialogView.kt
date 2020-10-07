package com.rlqja.toyou

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rlqja.toyou.CalendarActivity.Companion.context
import com.rlqja.toyou.adapter.PicturePageAdapter
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.annotations.RealmModule
import kotlinx.android.synthetic.main.click_memo_dialog.*
import kotlinx.android.synthetic.main.picture_sample.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class DialogView:AppCompatActivity() {
    lateinit var realm:Realm
    lateinit var memoData:MemoData
    lateinit var pictureList:RealmResults<PictureData>
    lateinit var adapter:PicturePageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.click_memo_dialog)
        memoData=intent.getParcelableExtra<MemoData>("memoData")
        Realm.init(this)
        val config = RealmConfiguration.Builder().modules(RealmModulePicture()).name("Picture.realm")
            .schemaVersion(4).build()
        realm = Realm.getInstance(config)
        pictureList=MemoClass(realm).getPictureById(memoData.id)
        dialogText.text=memoData.title
        dialogTime.text="${memoData.year}.${memoData.month} ${memoData.day}:${memoData.minute}"
        dialogContent.text=memoData.contents
        addPicture.setOnClickListener {
            val pictureIntent= Intent(Intent.ACTION_PICK)
            pictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            pictureIntent.type = "image/*"
            startActivityForResult(pictureIntent,300)
        }
        if(pictureList.size!=0){
            adapter= PicturePageAdapter(pictureList){picturedata,pictureBitmap->
                val pictureDialog=AlertDialog.Builder(this)
                val view=layoutInflater.inflate(R.layout.picture_sample,null)
                view.mainListPicture.setImageBitmap(pictureBitmap)
                pictureDialog.setNegativeButton("삭제",DialogInterface.OnClickListener { dialog, which ->
                    realm.executeTransaction {
                        picturedata.deleteFromRealm()
                        var tempData= arrayOf(picturedata)
                        pictureList.removeAll(tempData)
                    }
                    if(pictureList.size==0)
                        noPicture2.visibility=View.VISIBLE

                    dialog.dismiss()
                })
                pictureDialog.setPositiveButton("확인",DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                pictureDialog.setView(view)
                pictureDialog.show()
            }
            noPicture2.visibility=View.GONE
        }
        else adapter=PicturePageAdapter(pictureList){p,d->
        }

        picturePager.setPageTransformer(CalendarActivity().CalendarTransform())
        }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==300&&resultCode== Activity.RESULT_OK&&data!=null){
            val clipData=data?.clipData
            if(clipData!=null){
                val byteStream=ByteArrayOutputStream()
                var stream:InputStream?=null
                var pictureBitmap:Bitmap?
                val list= arrayOfNulls<PictureData>(clipData.itemCount)

                realm.executeTransaction {
                    for (i in 0..clipData.itemCount){
                        stream=context.contentResolver.openInputStream(clipData.getItemAt(i).uri)
                        pictureBitmap=BitmapFactory.decodeStream(stream)
                        pictureBitmap?.compress(Bitmap.CompressFormat.PNG,100,byteStream)
                        var tempData=PictureData(picture=byteStream.toByteArray(),distinguishId = memoData.id)
                        it.copyToRealm(tempData)
                        list[i]=tempData
                }
                    pictureList.addAll(list)
                }
                stream?.close()
                noPicture2.visibility=View.GONE
                adapter.notifyDataSetChanged()

            }
            else{
                Toast.makeText(this,"멀티 선택을 지원하지 않은 기기입니다",Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
@RealmModule(classes = [PictureData::class])
class RealmModulePicture