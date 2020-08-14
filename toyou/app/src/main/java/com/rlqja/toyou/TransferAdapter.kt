package com.rlqja.toyou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.transfer_list_inner_sample.view.*
import kotlinx.android.synthetic.main.transfer_list_sample.view.*


class TransferViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
class TransferAdapter(var data:ArrayList<TransitArray>,var width:Int,var height:Int,var itemClick:()->(Unit)):RecyclerView.Adapter<TransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transfer_list_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return data.size

    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        if(data[position].time>60) {
            holder.itemView.timeText.text = "${data[position].time/60}시간 ${data[position].time%60}분"
        }
        else holder.itemView.timeText.text="${data[position].time}분"
        holder.itemView.distanceText.text="${data[position].distance/1000.0}km"
        var manager=LinearLayoutManager(ChooseTransferList.context)
      manager.orientation=LinearLayoutManager.HORIZONTAL
        holder.itemView.transferlist.layoutManager=manager
        var end=LatLng(data[position].dataList[0].startX,data[position].dataList[0].startY)

       // var url=DirectionFinder().getWalkingDirectionUrl(LatLng(MainActivity.init_latitude!!,MainActivity.init_longitude!!),end)
       // DirectionFinder().execution(url)

        holder.itemView.transferlist.adapter=TransferInnerAdapter(data[position].dataList,width,height)
    }
}



class TransferInnerAdapter(var data:ArrayList<TransitData>,var width:Int,var height:Int):RecyclerView.Adapter<TransferViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transfer_list_inner_sample,parent,false))
    }

    override fun getItemCount(): Int {
       return data.size
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {

      holder.itemView.innerLayout.layoutParams=ConstraintLayout.LayoutParams(width/data.size,height)
        holder.itemView.directionImage.layoutParams=ConstraintLayout.LayoutParams(width/data.size,45)
       // holder.itemView.routeNum.layoutParams=ConstraintLayout.LayoutParams(width/data.size,45)
       // holder.itemView.lineNum.layoutParams=ConstraintLayout.LayoutParams(width/data.size,45)
        if(data[position].stationNum==0) {
            holder.itemView.transferImage.setImageResource(R.drawable.bus)
            holder.itemView.directionImage.setImageResource(R.drawable.pink_direction)
        }
        else{ holder.itemView.transferImage.setImageResource(R.drawable.subway)
            holder.itemView.directionImage.setImageResource(R.drawable.blue_direction)
        }
       // holder.itemView.transferImage.layout((width/data.size-48)/2,0,(width/data.size-48)/2,0)
        //holder.itemView.transferImage.setPadding((width/data.size-48)/2,0,(width/data.size-48)/2,0)
        holder.itemView.routeNum.text=data[position].startName
        //holder.itemView.routeNum.layoutParams=ConstraintLayout.LayoutParams(width/data.size, wrapContent)
        holder.itemView.lineNum.text=data[position].routeNum
    }

}