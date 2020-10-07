package com.rlqja.toyou.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rlqja.toyou.PictureData
import com.rlqja.toyou.R
import io.realm.RealmResults
import kotlinx.android.synthetic.main.picture_sample.view.*

class PicturePageAdapter(var urlList:RealmResults<PictureData>,var click:(PictureData, Bitmap)->(Unit)):RecyclerView.Adapter<DailyListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
            return DailyListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.picture_sample,parent,false))
        }

    override fun getItemCount(): Int =urlList.size
    override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {

            val pictureBitmap = BitmapFactory.decodeByteArray(urlList[position]?.picture,0,
                urlList[position]?.picture!!.size)
            holder.itemView.mainListPicture.setImageBitmap(pictureBitmap)
            holder.itemView.mainListPicture.setOnClickListener {
                click(urlList[position]!!,pictureBitmap)
            }


    }

}