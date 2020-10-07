package com.rlqja.toyou

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.android.synthetic.main.add_memo_on_calendar_activity.*
import java.util.*

class MemoOnCalendar:AppCompatActivity(),OnMapReadyCallback {
    lateinit var memoInstance:MemoData
    var map:NaverMap?=null
    var uniqueMarker:Marker?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_memo_on_calendar_activity)
        calendarMap.onCreate(savedInstanceState)
        timePicker.visibility=View.GONE
        val calendar=intent.getSerializableExtra("calendar") as Calendar
        val now=GregorianCalendar()
        memoInstance=MemoData()
        memoInstance.month=calendar.get(Calendar.MONTH)
        memoInstance.year=calendar.get(Calendar.YEAR)
        memoInstance.day=calendar.get(Calendar.DATE)
        memoInstance.hour=now.get(Calendar.HOUR)
        memoInstance.minute=now.get(Calendar.MINUTE)
        calendarMap.getMapAsync {
            map=it
            it.setOnMapLongClickListener { pointF, latLng ->
                moveCamera(latLng.latitude,latLng.longitude)
                memoInstance.latitude=latLng.latitude
                memoInstance.longitude=latLng.longitude
                locationCalendarText.text=DirectionFinder().convertToAddress(latLng.latitude,latLng.longitude)
            }
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_MEDIUM
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
            &&ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
        ) {
            manager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    if(location!=null){
                        moveCamera(location.latitude,location.longitude)
                    locationCalendarText.text=DirectionFinder().convertToAddress(location.latitude,location.longitude)}
                    else Toast.makeText(this@MemoOnCalendar,"위치 정보 가져오기 실패",Toast.LENGTH_SHORT).show()
                }
                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?) {}
                override fun onProviderEnabled(provider: String?) {}
                override fun onProviderDisabled(provider: String?) {}
            }, null)
        }

        locationFinder.setOnClickListener {
            val searchIntent= Intent(this,Search_List::class.java)
            searchIntent.putExtra("distributeMemoOrCalendar",1.toByte())
            startActivityForResult(searchIntent,20)
        }

       timerLayout.setOnClickListener {
           if(timePicker.visibility==View.VISIBLE) timePicker.visibility= View.GONE
           else timePicker.visibility=View.VISIBLE
        }
        chooseTimeText.text="시간 정하기 - ${calendar.get(Calendar.MONTH)+1}월 ${calendar.get(Calendar.DATE)}일 ${now.get(Calendar.HOUR)}:${now.get(Calendar.MINUTE)}"
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            chooseTimeText.text="시간 정하기 - ${calendar.get(Calendar.MONTH)+1}월 ${calendar.get(Calendar.DATE)}일 $hourOfDay:$minute"
            memoInstance.hour=hourOfDay
            memoInstance.minute=minute
        }
        timePicker.hour=now.get(Calendar.HOUR)
        timePicker.minute=now.get(Calendar.MINUTE)
            cancelCalendarButton.setOnClickListener {
                if(titleTextCalendar.text.toString()=="")
                    Toast.makeText(CalendarActivity.context,"해당 작업 내용은 취소되었습니다.",Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        saveCalendarButton.setOnClickListener {
            when {
                titleTextCalendar.text==null -> Toast.makeText(this,"제목을 입력해주세요",Toast.LENGTH_SHORT).show()
                memoInstance.hour==0 -> Toast.makeText(this,"시간을 입력해주세요",Toast.LENGTH_SHORT).show()
                memoInstance.latitude==0.0 -> Toast.makeText(this,"장소를 입력해주세요",Toast.LENGTH_SHORT).show()
                else -> {
                    memoInstance.memoOrCalendar=true
                    memoInstance.title=titleTextCalendar.text.toString()
                    CalendarActivity.realm.executeTransaction {
                        it.copyToRealm(memoInstance)
                    }
                    Toast.makeText(CalendarActivity.context,"저장되었습니다..",Toast.LENGTH_SHORT).show()
                    val calendarIntent=Intent(this,CalendarActivity::class.java)
                    setResult(100,calendarIntent)
                    finish()
                }
            }
        }

    }
    override fun onStart() {
        super.onStart()
        calendarMap.onStart()
    }

    override fun onResume() {
        super.onResume()
        calendarMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        calendarMap.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        calendarMap.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        calendarMap.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        calendarMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        calendarMap.onLowMemory()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==20&&resultCode==20){
            val latitude=data?.getDoubleExtra("latitude", 126.97935)
            val longitude=   data?.getDoubleExtra("longitude", 37.56398)
            val location=data?.getStringExtra("location")
            locationCalendarText.text="장소 : $location"
            moveCamera(latitude!!,longitude!!)
            memoInstance.latitude=latitude
            memoInstance.longitude=longitude

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun moveCamera(latitude:Double,longitude:Double){
        val camerUpdate =
            CameraUpdate.scrollTo(LatLng(latitude, longitude)).animate(CameraAnimation.Fly)
        val cameraPosition = CameraPosition(LatLng(latitude, longitude), 16.0)
        map?.cameraPosition = cameraPosition
        map?.moveCamera(camerUpdate)
        val marker= Marker()
        if(uniqueMarker!=null) uniqueMarker?.map=null
        uniqueMarker=marker
        marker.height=Marker.SIZE_AUTO
        marker.width=Marker.SIZE_AUTO
        marker.position= LatLng(latitude,longitude)
        marker.map=map
    }

    override fun onMapReady(p0: NaverMap) {
        map=p0
    }
}