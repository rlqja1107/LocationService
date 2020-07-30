package com.example.test


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.Location
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_sliding_inner.view.*
import org.json.JSONArray


class MainActivity : FragmentActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {
    companion object {
        lateinit var map: GoogleMap
        var polyline: ArrayList<Polyline> =ArrayList<Polyline>()
        var walkingTime: String = ""
        lateinit var nearbyMarker: ArrayList<KickBoardData>
        var context:Context?=null
    }

    lateinit var mapFragment: SupportMapFragment

    var init_latitude: Double? = null
    var init_longitude: Double? = null
    private var FINISHTIME = 2000
    private var backPressedTime = 0L
    private lateinit var manager: LocationManager
    private var markerCount = 0
    var uniqueMarker: Marker? = null
    var uniqueMarker1: Marker? = null
    lateinit var uniqueOverlay: GroundOverlay
    var permissionCheck = false
    lateinit var locationCallback: LocationCallback
    lateinit var mFusedLocationProvider: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    var startLocation: LatLng? = null
    var endLocation: LatLng? = null


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //위치를 켰는지 여부
        checkPermission()
        //아래 패널은 처음에 숨기게
       slidingPanel.panelState=SlidingUpPanelLayout.PanelState.HIDDEN

        //Map에 표시
        init_latitude = intent.getDoubleExtra("latitude", 37.56398)
        init_longitude = intent.getDoubleExtra("longitude", 126.97935)
        var initLatLng = LatLng(init_latitude!!, init_longitude!!)
        var checking = intent.getIntExtra("checkingPermission", 100)
        context=this

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
            addKickBoardMarker(it)

            //Info 설정
//            map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
//                override fun getInfoContents(p0: Marker?): View? {
//                    return null
//                }
//
//                override fun getInfoWindow(p0: Marker?): View {
//                    var view = layoutInflater.inflate(R.layout.marker_info, null)
//                    var latlng = p0?.tag as LatLng
//                    view.logoText.text = "${latlng.latitude},${latlng.longitude}"
//
//                    return view
//                }
//
//            })
            it.setOnMarkerClickListener { its ->
                if (its.tag != null) {
                    var lat = its.tag as LatLng
                    showPanel(lat.latitude,lat.longitude)
                }
                false
            }
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(initLatLng, 17F))
            //빌딩 3차원으로 나타내기
            it.isBuildingsEnabled = true
            uniqueOverlay = it.addGroundOverlay(
                GroundOverlayOptions().position(initLatLng, 80F, 80F).visible(true).image(
                    BitmapDescriptorFactory.fromResource(R.drawable.keyongsub)
                )
            )
            uniqueOverlay.tag = 0
            it.setOnMapLongClickListener { its ->
                putMarker(its.latitude, its.longitude, "Here we go~")
                showPanel(its.latitude, its.longitude)
            }
        }

        //var kickInstance = KickBoard()
        //toolbar 누르면 검색창으로 이동
        toolbar.setOnClickListener {
            var localActivity = Intent(this, LocationSearch::class.java)
            supportFragmentManager.popBackStack()
            startActivityForResult(localActivity, 20)
        }

        toolbar.setNavigationOnClickListener {
            MainDrawer.openDrawer(GravityCompat.START)
        }


        currentLocation.setOnClickListener {
            var alert = AlertDialog.Builder(this)
            alert.setMessage("현재 위치를 출발지로 하시겠습니까?")
            alert.setPositiveButton("예") { p0, p1 ->
                startLocation = LatLng(init_latitude!!, init_longitude!!)
                if (polyline.size>0) {
                    for(i in polyline) {
                        i.remove()
                    }
                    polyline.clear()
                }
                p0.dismiss()
            }
            alert.setNegativeButton("아니요") { p0, p1 ->
                p0.dismiss()
            }

            alert.show()
            moveCamera(init_latitude, init_longitude)
        }
        locationRequest =
            LocationRequest().setInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(2000)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                p0 ?: return
                var last = p0.lastLocation
                init_latitude = last.latitude
                init_longitude = last.longitude
                uniqueOverlay.position = LatLng(init_latitude!!, init_longitude!!)

                super.onLocationResult(p0)
            }
        }
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, null)

    }

    override fun onStart() {
        mFusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, null)
        super.onStart()
    }

    override fun onStop() {
        mFusedLocationProvider.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onBackPressed() {
        //closeButton()
        if(slidingPanel.panelState==SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingPanel.panelState=SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if(slidingPanel.panelState==SlidingUpPanelLayout.PanelState.COLLAPSED){
            slidingPanel.panelState=SlidingUpPanelLayout.PanelState.HIDDEN
            return
        }
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
            permissionCheck = true
            criteria.accuracy = Criteria.ACCURACY_MEDIUM
            criteria.powerRequirement = Criteria.POWER_MEDIUM
            manager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(p0: android.location.Location?) {
                    init_latitude = p0?.latitude
                    init_longitude = p0?.longitude
                    moveCamera(init_latitude, init_longitude)
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
            showPanel(latitude!!, longitude!!)
            moveCamera(latitude, longitude)
            putMarker(latitude, longitude, location!!)
        }
        //해당하는 킥보드부터의 경로찾기
        if(requestCode==5&&resultCode==5){
            var position=data?.getParcelableExtra<LatLng>("position")
            var destination=data?.getParcelableExtra<LatLng>("destination")
            var finder=DirectionFinder()
            var url=finder.getUrlByTmap(LatLng(init_latitude!!,init_longitude!!),destination!!,position!!)
            finder.FindWayByTmap(url).execute()
            moveCamera(init_latitude,init_longitude)
        }


        super.onActivityResult(requestCode, resultCode, data)
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

    private fun showPanel(latitude: Double, longitude: Double) {
        slidingPanel.panelState=SlidingUpPanelLayout.PanelState.EXPANDED
        addressText.text=DirectionFinder().convertToAddress(latitude,longitude)
            bottom_slide.endButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "목적지점이 지정되었습니다", Toast.LENGTH_SHORT).show()
            endLocation = LatLng(latitude, longitude)
        }
        bottom_slide.startButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "출발지점이 지정되었습니다", Toast.LENGTH_SHORT).show()
            startLocation = LatLng(latitude, longitude)
        }
        bottom_slide.findWay.setOnClickListener {
            if (endLocation == null || startLocation == null)
                Toast.makeText(this@MainActivity, "출발 지점 또는 목적지점을 지정하십시오", Toast.LENGTH_SHORT).show()
            else {

                val intentToList=Intent(this,ChooseTransferList::class.java)
                intentToList.putExtra("start",startLocation)
                intentToList.putExtra("destination",endLocation)
                startActivityForResult(intentToList,5)
//                var direction = Direction_Finder()
//                var url = direction.getDirectionUrl(startLocation!!, endLocation!!)
//                direction.GetDirection(url).execute()
//                Handler().postDelayed({
//                    if(showTime.text!="")
//                    showTime.text= walkingTime
//                    showTime.visibility=View.VISIBLE
//                    Handler().postDelayed({
//                        showTime.visibility=View.GONE
//                    }, walkingTime.replace("분","").toLong())
//                },3000)
            }
        }
    }

    private fun moveCamera(latitude: Double?, longitude: Double?) {
        var camerUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 17F)
        map.animateCamera(camerUpdate)
    }

    var total = 0
    private fun putMarker(latitude: Double, longitude: Double, title: String) {
        if (markerCount == 2) {
            if (total % 2 == 0) {
                uniqueMarker?.remove()
                uniqueMarker = null
                markerCount--
            } else {
                uniqueMarker1?.remove()
                uniqueMarker1 = null
                markerCount--
            }
            total++
        }
        if (markerCount <= 2) {
            if (polyline.size>0) {
                for(i in polyline)
                    i.remove()
                polyline.clear()
            }
            if (uniqueMarker == null) {
                uniqueMarker = map.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            latitude,
                            longitude
                        )
                    ).draggable(false)
                )
                uniqueMarker?.tag = LatLng(latitude, longitude)
                markerCount++
            } else if (uniqueMarker1 == null) {
                uniqueMarker1 = map.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            latitude,
                            longitude
                        )
                    ).draggable(false)
                )
                uniqueMarker1?.tag = LatLng(latitude, longitude)
                markerCount++
            }
        }
    }

    private fun checkPermission(): Boolean {
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

    //json파일로 좌표찍기
    private fun addKickBoardMarker(map: GoogleMap): String {
        var json = ""
        try {
            var stream = application.resources.assets
            val input = stream.open("data1.json")
            val jsonString = input.bufferedReader().use { it.readText() }
            val jobject = JSONArray(jsonString)

            var location1 = Location("start")
            location1.latitude = init_latitude!!
            location1.longitude = init_longitude!!
            nearbyMarker = ArrayList<KickBoardData>()
            var distance = 0F
            var lat: Double? = null
            var lng: Double? = null
            var location2: Location? = null
            var company:String?=null
            var battery:String?=null
            for (i in 0 until jobject.length()) {
                val obj = jobject.getJSONObject(i)
                lat = obj.getString("Latitude").toDouble()
                lng = obj.getString("Longitude").toDouble()
                company=obj.getString("Company")
                battery=obj.getString("Battery")
                location2 = Location("destination")
                location2.latitude = lat
                location2.longitude = lng
                distance = location1.distanceTo(location2)
                if (distance < 200) {
                    whichIcon(company,LatLng(lat,lng),battery,distance)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return json
    }
    private fun whichIcon(company:String,latlng:LatLng,battery:String,distance:Float){
        var resource:BitmapDescriptor?=null
        when(company){
            "Swing"->{
                resource=BitmapDescriptorFactory.fromResource(R.drawable.swing)
            }
            "Deer"->{
                resource=BitmapDescriptorFactory.fromResource(R.drawable.deer)

            }
            "Kickgoing"->{
                resource=BitmapDescriptorFactory.fromResource(R.drawable.kickgoing)

            }
            "Beam"->{
                resource=BitmapDescriptorFactory.fromResource(R.drawable.beam)

            }
            "XingXing"->{
                resource=BitmapDescriptorFactory.fromResource(R.drawable.xingxing)
            }
        }
        nearbyMarker.add(KickBoardData(map.addMarker(MarkerOptions().position(latlng).title(battery)
            .icon(resource)),0,distance.toInt(),company))

    }

}
