package com.example.test


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener{
    companion object{
        var mapKickBoard=ArrayList<KickBoardData>()
    }
    lateinit var mapFragment: SupportMapFragment
    lateinit var map: GoogleMap
    var init_latitude: Double? = null
    var init_longitude: Double? = null
    final private var FINISHTIME = 2000
    private var backPressedTime = 0L
    lateinit var manager: LocationManager
    var markerCount = 0
    lateinit var uniqueMarker: Marker
    lateinit var uniqueOverlay: GroundOverlay
    var permissionCheck = false
    lateinit var locationCallback:LocationCallback
    lateinit var mFusedLocationProvider: FusedLocationProviderClient
    lateinit var locationRequest:LocationRequest

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //위치를 켰는지 여부
        checkPermission()



        //Map에 표시

        init_latitude = intent.getDoubleExtra("latitude", 37.56398)
        init_longitude = intent.getDoubleExtra("longitude", 126.97935)
        var initLatLng = LatLng(init_latitude!!, init_longitude!!)
        var checking = intent.getIntExtra("checkingPermission", 100)
        //위치 수신을 얼마나 정확하게, 간격은 어떻게 할 것인지

        //위치권한 체크가 안되어있으면 켜기
        if (permissionCheck) {
            requestPermission()
        }

        //checking==0이면 snackbar로 권한 체크 유도(위치기능 켜기)
        if (checking == 0) {
            Snackbar.make(
                MainDrawer,
                "폰의 위치기능을 켜야 사용할 수 있습니다.",
                10000
            ).setAction("설정", View.OnClickListener {
                val goSetting = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(goSetting, 100)
            }).show()
        }
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        //초기 화면 설정
        mapFragment.getMapAsync {
            map = it
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(initLatLng, 17F))
            //빌딩 3차원으로 나타내기
            it.isBuildingsEnabled = false
            uniqueOverlay = it.addGroundOverlay(
                GroundOverlayOptions().position(initLatLng, 80F, 80F).visible(true).image(BitmapDescriptorFactory.fromResource(R.drawable.keyongsub))
            )
            uniqueOverlay.tag = 0
            var a= uniqueOverlay

            it.setOnMapLongClickListener { its ->
                putMarker(its.latitude, its.longitude, "Here we go~")
            }
        }
        var kickInstance=KickBoard()
        //Deer 위치정보 가져오기
        //kickInstance.deerLocation()
        //kickInstance.pushToMap(map)
        kickInstance.beamLocation()

        //toolbar 누르면 검색창으로 이동
        toolbar.setOnClickListener {

            var localActivity = Intent(this, LocationSearch::class.java)
            startActivityForResult(localActivity, 20)
        }

        toolbar.setNavigationOnClickListener {

            MainDrawer.openDrawer(GravityCompat.START)
        }
        //건물을 3차원적으로

        currentLocation.setOnClickListener {
            moveCamera(init_latitude, init_longitude)
        }
        locationRequest=LocationRequest().setInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(2000)
        locationCallback=object:LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                p0?: return
                var last=p0.lastLocation
                uniqueOverlay.position= LatLng(last.latitude,last.longitude)

                    super.onLocationResult(p0)
            }
        }

        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProvider.requestLocationUpdates(locationRequest,locationCallback,null)

    }

    override fun onStart() {
        mFusedLocationProvider.requestLocationUpdates(locationRequest,locationCallback,null)
        super.onStart()
    }
    override fun onStop() {
        mFusedLocationProvider.removeLocationUpdates(locationCallback)
        super.onStop()
    }
    override fun onBackPressed() {
        if (MainDrawer.isDrawerOpen(GravityCompat.START))
            MainDrawer.closeDrawer(GravityCompat.START)
        else {
            var tempTime = System.currentTimeMillis()
            var intervalTime = tempTime - backPressedTime
            if (intervalTime in 0..FINISHTIME) super.onBackPressed()
            else {
                backPressedTime = tempTime
                Toast.makeText(this, "종료하려면 한 번 더 뒤로가기 누르세요", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!

    }

    //Navigation item 선택시
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        p0.isChecked = true
        MainDrawer.closeDrawers()
        when (p0.itemId) {
            R.id.Login -> {

            }
            R.id.Mypage -> {

            }
            R.id.Setting -> {

            }
            R.id.Notification -> {

            }
        }
        MainDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //위치 설정 킨 후, 자기 위치로 카메라 이동
        if (requestCode == 10 && checkPermission()) {
            val criteria = Criteria()
            permissionCheck=true
            criteria.accuracy = Criteria.ACCURACY_MEDIUM
            criteria.powerRequirement = Criteria.POWER_MEDIUM
            manager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(p0: Location?) {
                    init_latitude = p0?.latitude
                    init_longitude = p0?.longitude
                    moveCamera(p0?.latitude, p0?.longitude)
                }

                override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                }

                override fun onProviderEnabled(p0: String?) {
                }

                override fun onProviderDisabled(p0: String?) {
                }
            }, null)
        }

        //검색 후 카메라 이동
        if (requestCode == 20 && resultCode == 20) {
            var latitude = data?.getDoubleExtra("latitude", init_latitude!!)
            var longitude = data?.getDoubleExtra("longitude", init_longitude!!)
            var location = data?.getStringExtra("location")
            moveCamera(latitude, longitude)
            putMarker(latitude!!, longitude!!, location!!)
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    //마커 눌렀을 때
    override fun onMarkerClick(p0: Marker?): Boolean {
        if (p0?.tag == 0) {

        }
        return true
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
            Toast.makeText(this, "앱을 실행시키려면 위치 권한이 필요합니다", Toast.LENGTH_LONG).show()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), 200
        )
    }

    private fun moveCamera(latitude: Double?, longitude: Double?) {
        var camerUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 17F)
        map.animateCamera(camerUpdate)
    }

     fun putMarker(latitude: Double, longitude: Double, title: String) {
        if (markerCount == 1) {
            uniqueMarker.remove()
            markerCount--
        }
        if (markerCount <= 0) {
            uniqueMarker = map.addMarker(
                MarkerOptions().position(
                    LatLng(
                        latitude,
                        longitude
                    )
                ).draggable(false).title(title)
            )
            uniqueMarker.tag = 0
            markerCount++
        }
    }

    private fun checkPermission():Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionCheck = true
        }
        return permissionCheck
    }
    private fun getDeviceLocation(){
        if(permissionCheck){
            var locationResult=mFusedLocationProvider.lastLocation
            locationResult.addOnCompleteListener(this@MainActivity
            ) { p0 ->
                if(p0.isSuccessful){
                    var mLastLocation=p0.result

                }
            }
        }
    }



}
