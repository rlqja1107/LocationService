package com.example.test

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import kotlinx.android.synthetic.main.inner_transfer_sample.view.*
import kotlinx.android.synthetic.main.transfer_list_sample.view.*


class TransferViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
class TransferAdapter(var data:ArrayList<ArrayList<TransferData>>):RecyclerView.Adapter<TransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transfer_list_sample,parent,false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        holder.itemView.timeListText.text = ChooseTransferList.duration
        holder.itemView.walkingTime.text ="${data[position][0].duration.toInt()}분"
        holder.itemView.walking_html.text ="${data[position][0].word}"
        Glide.with(ChooseTransferList.context).load(R.drawable.walking2)
            .into(GlideDrawableImageViewTarget(holder.itemView.walkingImage))
        var transferTemp = data[position].filter{
            it.mode=="TRANSIT"
        }

        var manager = LinearLayoutManager(ChooseTransferList.context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        holder.itemView.innerTransferLayout.layoutManager = manager
        holder.itemView.innerTransferLayout.adapter = InnerTransferRecyclerView(transferTemp)



    }


}

class InnerTransferViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

//내부 recyclerview
class InnerTransferRecyclerView(var trafficData:List<TransferData>):RecyclerView.Adapter<InnerTransferViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerTransferViewHolder {
        var view=LayoutInflater.from(parent.context).inflate(R.layout.inner_transfer_sample,parent,false)
        return InnerTransferViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trafficData.size
    }

    override fun onBindViewHolder(holder: InnerTransferViewHolder, position: Int) {
        if(trafficData[position].transit.line.vehicle.type=="BUS"){
            holder.itemView.traffiImage.setImageResource(R.drawable.bus)
            holder.itemView.trafficStop.text="${trafficData[position].transit.num_stops}정거장"
        }
        else if(trafficData[position].transit.line.vehicle.type=="SUBWAY"){
            holder.itemView.traffiImage.setImageResource(R.drawable.subway2)
            holder.itemView.trafficStop.text="${trafficData[position].transit.num_stops}역"
        }
        holder.itemView.trafficName.text=trafficData[position].transit.line.short_name
        holder.itemView.trafficName.setTextColor(Color.parseColor(trafficData[position].transit.line.color))
        holder.itemView.departureText.text=trafficData[position].transit.departure_stop.name
        holder.itemView.arrivalText.text=trafficData[position].transit.arrival_stop.name


    }

}