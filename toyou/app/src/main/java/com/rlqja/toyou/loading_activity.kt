package com.rlqja.toyou


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.first_show.*

class loading_activity:AppCompatActivity() {
    private var permission:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_show)

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_MEDIUM
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        val mainActivity = Intent(this, MainActivity::class.java)
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled &&!isNetworkEnabled&&!permission) {
            goMainActivity(mainActivity,2)
        }
        else if(!isGPSEnabled &&!isNetworkEnabled&&permission){
            goMainActivity(mainActivity,1)
        }
        else if(checkPermission()){
            //다 켜져있는 경우
            manager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    mainActivity.putExtra("latitude", location?.latitude)
                    mainActivity.putExtra("longitude", location?.longitude)
                    //1을 보내면 위치설정되어있음
                    mainActivity.putExtra("checkingPermission", 3)
                    startActivity(mainActivity)
                    finish()
                }

                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String?) {
                }
            }, null)
        }
        else{
            //위치권한이 안켜져있는경우
            goMainActivity(mainActivity,0)
        }


    }
    fun checkPermission():Boolean{
        val finePermission=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
        val coarsePermission=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
        return finePermission&&coarsePermission
    }

    private fun goMainActivity(mainActivity:Intent, check:Int){
        var handler = Handler()
        handler.postDelayed({
            mainActivity.putExtra("moveLatitude",0.0)
            mainActivity.putExtra("moveLongitude",0.0)
            mainActivity.putExtra("checkingPermission", check)
            mainActivity.putExtra("latitude", 37.56398)
            mainActivity.putExtra("longitude", 126.97935)
            startActivity(mainActivity)
            finish()
        }, 2000)
    }
}