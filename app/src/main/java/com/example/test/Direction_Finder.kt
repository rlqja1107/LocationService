package com.example.test

import android.graphics.Color
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

class Direction_Finder {
    fun getDirectionUrl(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=transit&departure_time=now&language=ko&key=AIzaSyD0982tla5btYfpYCWfH23NRXFU7pirdWc"
    }

    inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body.toString()
            val result = ArrayList<List<LatLng>>()
            try {
                val obj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                for (i in 0 until obj.routes[0].legs[0].steps.size) {
                    val startlatLng = LatLng(
                        obj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        obj.routes[0].legs[0].steps[i].start_location.lng.toDouble()
                    )
                    path.add(startlatLng)
                    val endlatLng = LatLng(
                        obj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
                        obj.routes[0].legs[0].steps[i].end_location.lng.toDouble()
                    )
                    path.add(endlatLng)
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>?) {
            if (result != null) {
                val lineoption=PolylineOptions()
                for(i in result.indices){
                       lineoption.addAll(result[i])
                        .width(10f)
                        .color(Color.BLUE)
                        .geodesic(true)
                }
                MainActivity.map.addPolyline(lineoption)
            }
        }

    }
}