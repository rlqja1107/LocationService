package com.rlqja.toyou


import android.location.Geocoder
import android.os.AsyncTask

import com.naver.maps.geometry.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.ArrayList

data class TransferData(var distance:Int,var duration:Double,var start:LatLng,var end:LatLng,var word:String="",var mode:String,var transit:Transit)
data class TransitData(var startName:String="",var startX:Double=0.0,var startY:Double=0.0,var routeNum:String="",var endName:String="",var endX:Double=0.0,var endY:Double=0.0,var stationNum:Int=0)
data class TransitArray(var dataList:ArrayList<TransitData>,var time:Int=0,var distance:Int)

class DirectionFinder {
    fun getODsayUrl(origin: LatLng,dest:LatLng):String{
        return "https://api.odsay.com/v1/api/searchPubTransPath?SX=${origin.longitude}&SY=${origin.latitude}&EX=${dest.longitude}&EY=${dest.latitude}&apiKey=751H77J/gCws2AqqBTjkJwdYA1b7OSxY6f3gsriXsTY"
    }
    fun execution(url:String)
    {
        var client=OkHttpClient()
        var request=Request.Builder().url(url).build()
        var response=client.newCall(request).execute()
        var data=response.body().toString()
        try{
            var jobject=JSONObject(data)
        }
        catch(e:Exception){

        }
    }
    fun getWalkingDirectionUrl(origin: LatLng,dest: LatLng):String{
        return "https://api.mapbox.com/directions/v5/mapbox/walking/${origin.longitude},${origin.latitude};${dest.longitude},${dest.latitude}?access_token=pk.eyJ1Ijoia2lta2lidW0iLCJhIjoiY2tkOHQ5bzAyMGowZTJzazZxNTAwMDhxYiJ9.DedrgGuwnH_R0oU8HDMEZg"
    }
    fun getPublicDataUrl(origin:LatLng,dest: LatLng): String {
        return "http://ws.bus.go.kr/api/rest/pathinfo/getPathInfoByBusNSub?ServiceKey=ni9VQODwzGpRojq47XEsS7onMl0VqL9Kux%2FfdAMaI1Hy2Twgvfcj%2FDPsCrtewJNjALyxGGoN1B8psN9bIiH62A%3D%3D&startX=${origin.longitude}&startY=${origin.latitude}&endX=${dest.longitude}&endY=${dest.latitude}"
    }
    fun getDirectionUrl(origin: LatLng, dest: LatLng,pass:ArrayList<LatLng>): String {

        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&waypoinst=&mode=transit&departure_time=now&language=ko&key=AIzaSyD0982tla5btYfpYCWfH23NRXFU7pirdWc"
        //        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=walking&key=AIzaSyD0982tla5btYfpYCWfH23NRXFU7pirdWc"

    }
    fun getUrlByNaver(origin: LatLng,dest:LatLng):String{
       return "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=${origin.longitude},${origin.latitude}&goal=${dest.longitude},${dest.latitude}&"+
               "option=traavoidcaronly&X-NCP-APIGW-API-KEY-ID=w2gacxjh1s&X-NCP-APIGW-API-KEY=ck3E1THnUhQY0C3prds1jIgKdOOTjY19YzfOISxY"
    }
    fun getUrlByTmap(origin: LatLng,dest: LatLng,pass:LatLng):String{
        return "https://apis.openapi.sk.com/tmap/routes/pedestrian?appKey=l7xxe7f1d6ac2e2e4f30a763b733810adea7&startX=${origin.longitude}&startY=${origin.latitude}&endX=${dest.longitude}&endY=${dest.latitude}&passList=${pass.longitude},${pass.latitude}&startName=go&endName=end"
    }
    fun convertToAddress(latitude:Double,longitude: Double):String{
        var geo=Geocoder(MainActivity.context)
        var address= geo.getFromLocation(latitude,longitude,1)
        if(address.size==0)
            return "해당되는 주소 정보는 없습니다."
        else {
            var addressline=address[0].getAddressLine(0).replace("대한민국","")

            return addressline
        }

    }
    inner class FindWayByTmap(var url:String):AsyncTask<Void,Void,List<LatLng>>(){
        override fun doInBackground(vararg p0: Void?): List<LatLng> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
            var result=ArrayList<LatLng>()
            try{
                val obj=JSONObject(data)
                val arr=obj.getJSONArray("features")
                for(i in 0 until arr.length()){
                    val obj2=arr[i] as JSONObject
                    println("obj2 : $obj2")
                    val coordinate=obj2.getJSONObject("geometry").getJSONArray("coordinates")
                    for(j in 0 until coordinate.length()){
                        if(coordinate[j] is Double){
                             result.add(LatLng(coordinate[1] as Double,coordinate[0] as Double))
                        }
                        else{
                            var location=coordinate[j] as JSONArray

                            result.add(LatLng(location[1] as Double,location[0] as Double))

                        }

                    }
                }

            }
            catch(e:Exception){
                e.printStackTrace()
            }
            return result
        }
        var colorflag=true
        override fun onPostExecute(result: List<LatLng>?) {
        }

    }


    inner class FindWayByNaver(var url:String):AsyncTask<Void,Void,List<LatLng>>(){
        override fun doInBackground(vararg p0: Void?): List<LatLng> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()


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

        }
    }


}