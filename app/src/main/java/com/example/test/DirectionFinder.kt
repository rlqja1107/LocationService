package com.example.test


import android.graphics.Color
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

data class TransferData(var distance:Int,var duration:Double,var start:LatLng,var end:LatLng,var word:String="",var mode:String,var transit:Transit)

class DirectionFinder {

    fun getDirectionUrl(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=transit&departure_time=now&language=ko&key=AIzaSyD0982tla5btYfpYCWfH23NRXFU7pirdWc"
        //        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=walking&key=AIzaSyD0982tla5btYfpYCWfH23NRXFU7pirdWc"

    }
    fun getUrlByNaver(origin: LatLng,dest:LatLng):String{
       return "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=${origin.longitude},${origin.latitude}&goal=${dest.longitude},${dest.latitude}&"+
               "option=traavoidcaronly&X-NCP-APIGW-API-KEY-ID=w2gacxjh1s&X-NCP-APIGW-API-KEY=ck3E1THnUhQY0C3prds1jIgKdOOTjY19YzfOISxY"
    }



    inner class FindWayByNaver(var url:String):AsyncTask<Void,Void,List<LatLng>>(){
        override fun doInBackground(vararg p0: Void?): List<LatLng> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body?.string()


            val result = ArrayList<LatLng>()
            try {
                val obj = JSONObject(data)


                val arr=obj.getJSONObject("route").getJSONArray("traavoidcaronly")[0] as JSONObject
                var loadArray=arr["path"] as JSONArray
               for(i in 0 until loadArray.length()){
                   var element=loadArray[i] as JSONArray
                   result.add(LatLng(element[1] as Double,element[0] as Double))
               }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<LatLng>?) {
            if (result != null) {
                var option=PolylineOptions()
                for (i in result?.listIterator()) {
                    option.add(i)
                        .width(15F)
                        .color(Color.GREEN)
                        .geodesic(true)
                }
                MainActivity.polyline= MainActivity.map.addPolyline(option)

            }
        }
    }


}