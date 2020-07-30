package com.example.toyou


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

class loading_activity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_show)
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_MEDIUM
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        var mainActivity = Intent(this, MainActivity::class.java)
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled && !isNetworkEnabled) {
            //GPS, network 둘다 안켜져있는 경우
            goMainActivity(mainActivity,2)
        } else {

            if (checkPermission()) {
                manager.requestSingleUpdate(criteria, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        mainActivity.putExtra("latitude", location?.latitude)
                        mainActivity.putExtra("longitude", location?.longitude)
                        //1을 보내면 위치설정되어있음
                        mainActivity.putExtra("checkingPermission", 1)
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
            //위치 권한은 있지만, 위치가 안켜져있는 경우
            else{
                goMainActivity(mainActivity,0)
            }
        }

    }
    private fun checkPermission():Boolean{
        val finePermission=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
        val coarsePermission=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED

        return finePermission&&coarsePermission
    }
    //위치권한이 없는 경우, Activity 넘어가는 함수
    private fun goMainActivity(mainActivity:Intent, check:Int){
        var handler = Handler()
        handler.postDelayed({
            //Permission이 안되어있으면 0
            mainActivity.putExtra("checkingPermission", check)
            mainActivity.putExtra("latitude", 37.56398)
            mainActivity.putExtra("longitude", 126.97935)
            startActivity(mainActivity)
            finish()
        }, 2000)
    }
}