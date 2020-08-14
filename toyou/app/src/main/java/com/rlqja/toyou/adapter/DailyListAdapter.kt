package com.rlqja.toyou.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rlqja.toyou.MemoData
import com.rlqja.toyou.R
import kotlinx.android.synthetic.main.daily_list_sample.view.*

class DailyListViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){}
class DailyListAdapter(var dailyList:ArrayList<MemoData>):RecyclerView.Adapter<DailyListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
        return DailyListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.daily_list_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return dailyList.size
    }

    override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {
        holder.itemView.dailyText.text=dailyList[position].title
    }
}