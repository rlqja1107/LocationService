package com.rlqja.toyou.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rlqja.toyou.MemoData
import com.rlqja.toyou.R
import io.realm.RealmResults
import kotlinx.android.synthetic.main.main_dailylist_sample.view.*

class DailyListAdapter2(var checkWord:String,var todayMemo:RealmResults<MemoData>,var click:(MemoData)->Unit): RecyclerView.Adapter<DailyListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
        return DailyListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_dailylist_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return if(checkWord=="") todayMemo.size
        else 1
    }

    override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {
        if(todayMemo.size>0) {
            holder.itemView.mainTitle.text = "${position + 1}. ${todayMemo[position]?.title}"
            holder.itemView.mainDay.text =
                "Time: ${todayMemo[position]?.hour}시 ${todayMemo[position]?.minute}"
            holder.itemView.mainListSample.setOnClickListener{
                click(todayMemo[position]!!)
            }
        }
        else{
            holder.itemView.mainTitle.text=checkWord
            holder.itemView.mainDay.text="일정을 등록해주세요"
        }
    }

}