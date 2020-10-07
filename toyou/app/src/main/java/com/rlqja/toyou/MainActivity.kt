package com.rlqja.toyou


import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.link.LinkClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.rlqja.toyou.adapter.DailyListAdapter2
import com.rlqja.toyou.adapter.DailyListViewHolder
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.realm.*
import io.realm.annotations.RealmModule
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_first_indicator_show.view.*
import kotlinx.android.synthetic.main.main_second_indicator_show.view.*
import kotlinx.android.synthetic.main.show_picture_map.view.*
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        var context: Context? = null
        var init_latitude: Double? = null
        var init_longitude: Double? = null
    }

    private lateinit var mapView: MapView
    lateinit var locationSource: FusedLocationSource
    var markerCount = 0
    var endLocation: LatLng? = null
    var startLocation: LatLng? = null
    var uniqueMarker1: Marker? = null
    var uniqueMarker2: Marker? = null
    lateinit var manager: LocationManager
    private var naverMap: NaverMap? = null
    var memo = MemoData()
    var realm: Realm? = null
    var memodata: RealmResults<MemoData>? = null
    lateinit var currentMemoArray: ArrayList<MemoData>
    var startFlag: Byte = 0
    var mode: Boolean = true
    var pictureFlag: Bitmap? = null
    lateinit var cal: GregorianCalendar
    lateinit var confirmButton: Button
    lateinit var deleteButton:Button
    lateinit var galleryImage:ImageView
    lateinit var timerImage:ImageView
    lateinit var memoButton:Button
    lateinit var calendar:Button
    lateinit var titleText:EditText
    lateinit var contentText:EditText
    lateinit var startButton:Button
    lateinit var endButton: Button
    lateinit var indicateText:TextView
    lateinit var mainRecyclerView2:RecyclerView
    private val markerlist=ArrayList<InfoWindow>()
    private var backPressedTime:Long=0
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KakaoSdk.init(this, resources.getString(R.string.kakao_appkey))
        cal = GregorianCalendar()
        val adapter=MainPagerAdapter()
        bottom_slide.adapter=adapter
        bottom_slide.currentItem=1

        //초기 세팅
        Realm.init(this)
        val config = RealmConfiguration.Builder().modules(RealmModuleMemo()).name("Memo.realm")
            .schemaVersion(4).build()

        realm = Realm.getInstance(config)
        context = this
        currentMemoArray = ArrayList<MemoData>()

        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        slidingPanel.addPanelSlideListener(object:SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                if(newState==SlidingUpPanelLayout.PanelState.HIDDEN||newState==SlidingUpPanelLayout.PanelState.COLLAPSED)
                    mainBottomNavigation.visibility=View.VISIBLE
                else{
                    mainBottomNavigation.visibility=View.INVISIBLE
                    showTitle.visibility=View.VISIBLE
                    toolbar.visibility=View.VISIBLE
                }
            }
        })
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        //데이터베이스로 메모정보가져오기

        memodata = MemoClass(realm!!).getAllMemo()

        for(data in memodata!!.iterator()){
            val date=GregorianCalendar(data.year,data.month+1,data.day,data.hour,data.minute)
            if(cal.after(date)&&data.memoOrCalendar){
                realm!!.executeTransaction {
                    data.isDatePassed=true
                }
            }
        }
        setSupportActionBar(toolbar)
        toolbar.title = ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        locationSource = FusedLocationSource(this, 10)
        init_latitude = intent.getDoubleExtra("latitude", 37.56398)
        init_longitude = intent.getDoubleExtra("longitude", 126.97935)
        val moveLatitude = intent.getDoubleExtra("moveLatitude", 0.0)
        val moveLongitude = intent.getDoubleExtra("moveLongitude", 0.0)
        val checking = intent.getIntExtra("checkingPermission", 100)

        toolbar.setOnClickListener {
            val localActivity = Intent(this, Search_List::class.java)
            startActivityForResult(localActivity, 20)
        }
        //GPS, 위치 권한을 받았는지 check후 초기세팅
        when (checking) {
            2 -> {
                requestPermission()
                requestLocation()
                initSetting(
                    mapView,
                    init_latitude,
                    init_longitude,
                    0,
                    memodata!!,
                    moveLatitude,
                    moveLongitude
                )
            }
            0 -> {
                requestPermission()
                initSetting(
                    mapView,
                    init_latitude,
                    init_longitude,
                    0,
                    memodata!!,
                    moveLatitude,
                    moveLongitude
                )
            }
            1 -> {
                requestLocation()
                initSetting(
                    mapView,
                    init_latitude,
                    init_longitude,
                    0,
                    memodata!!,
                    moveLatitude,
                    moveLongitude
                )
            }
            else -> {
                initSetting(
                    mapView,
                    init_latitude,
                    init_longitude,
                    1,
                    memodata!!,
                    moveLatitude,
                    moveLongitude
                )
            }
        }
        currentLocation.setOnClickListener {
            moveCamera(init_latitude, init_longitude)
        }
        navigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Login -> {
                }
                R.id.Mypage -> {
                }
                R.id.Setting -> {

                    LinkClient.instance.customTemplate(this, 34322) { linkResult, error ->
                        if (error != null) Toast.makeText(
                            this,
                            "카카오링크보내기 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                        else if (linkResult != null) {
                            startActivity(linkResult.intent)
                        }
                    }
                }
                R.id.Notification -> {
                }
            }
            Main_drawer.closeDrawers()
            true
        }
        bottom_slide.currentItem=1
        mainBottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.onlyMemo->{
                    showCalendarOrMemo(true)
                    true
                }
                R.id.onlyCalendar->{
                    showCalendarOrMemo(false)
                    true
                }
                R.id.allCanLook->{
                    showCalendarOrMemo(null)
                    true
                }
                else-> true
            }
        }

    }
    private fun showCalendarOrMemo(which:Boolean?){
        if(which==null){
            for(data in markerlist)
                data.map=naverMap
        }
        else {
            for (data in markerlist) {
                if (data.tag == which)
                    data.map = null
                else data.map=naverMap
            }
        }
    }
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        realm?.close()
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == 10) {
            moveCamera(init_latitude, init_longitude)
        }
        //대중교통 목록을 검색후
        else if (requestCode == 5 && resultCode == 5) {
            startLocation = null
            endLocation = null
        }
        //위치검색 후 결과
        else if (requestCode == 20 && resultCode == 20) {
            val latitude = data?.getDoubleExtra("latitude", init_latitude!!)
            val longitude = data?.getDoubleExtra("longitude", init_longitude!!)
            showTitle.text = data?.getStringExtra("location")
            moveCamera(latitude, longitude)
            val mark = putMarker(latitude, longitude)
            showPanel(latitude!!, longitude!!, false, null, mark, false)
        }
        //달력을 눌렀을 때
        else if (resultCode == 30 && requestCode == 30) {
            val latitude = data?.getDoubleExtra("latitude", init_latitude!!)
            val longitude = data?.getDoubleExtra("longitude", init_longitude!!)
            moveCamera(latitude, longitude)
        }
        else if(requestCode==30&&resultCode==40){
            for(remove in CalendarActivity.removeList){
                val info= markerlist.find {
                    it.position==LatLng(remove.latitude,remove.longitude)
                }
                info?.map=null
            }
        }
        //사진 가져올때
        else if (requestCode == 150 && resultCode == Activity.RESULT_OK) {
            try {
                val stream = contentResolver.openInputStream(data?.data!!)
                val img = BitmapFactory.decodeStream(stream)
                pictureFlag = img
                stream?.close()
                indicateText.text = "사진 설정 완료"
                galleryImage.setImageResource(R.drawable.picture_click)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            if (!checkPermission()) {
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            } else {
                naverMap?.locationTrackingMode = LocationTrackingMode.Face
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_MEDIUM
                criteria.powerRequirement = Criteria.POWER_MEDIUM

                manager.requestSingleUpdate(criteria, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        init_latitude = location?.latitude
                        init_longitude = location?.longitude
                        overlaySetting(init_latitude!!, init_longitude!!)
                        moveCamera(init_latitude, init_longitude)
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

            return
        }
        //앨범 허용했을때 갤러리로 가지게끔
        else if (requestCode == 150) {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(galleryIntent, 150)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.first_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            return
        }
        if (Main_drawer.isDrawerOpen(GravityCompat.START)) {
            Main_drawer.closeDrawer(GravityCompat.START)
        } else {
            val tempTime=System.currentTimeMillis()
            val intervalTime=tempTime-backPressedTime
            if(intervalTime>=0&&intervalTime>=2000){
                realm?.close()
                super.onBackPressed()
            }
            else{
                backPressedTime=tempTime
                Toast.makeText(this,"뒤로가기 한 번더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()

            }
        }
    }

    //toolbar에 있는 menu 행동을 정함
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {//왼쪽 상단 버튼을 눌렀을 때
                Main_drawer.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //카메라 움직이기
    private fun moveCamera(latitude: Double?, longitude: Double?) {
        val cameraUpdate =
            CameraUpdate.scrollTo(LatLng(latitude!!, longitude!!)).animate(CameraAnimation.Fly)
        val cameraPosition = CameraPosition(LatLng(latitude, longitude), 16.0)
        naverMap?.cameraPosition = cameraPosition
        naverMap?.moveCamera(cameraUpdate)
    }

    private fun overlaySetting(latitude: Double, longitude: Double) {
        val locationLay = naverMap?.locationOverlay
        locationLay?.isVisible = true
        locationLay?.iconWidth = LocationOverlay.SIZE_AUTO
        locationLay?.iconHeight = LocationOverlay.SIZE_AUTO
        locationLay?.position = LatLng(latitude, longitude)
        locationLay?.circleRadius = 200
        naverMap?.locationTrackingMode = LocationTrackingMode.Face
    }

    private fun initSetting(
        mapView: MapView,
        latitude: Double?,
        longitude: Double?,
        checking: Int,
        memoData: RealmResults<MemoData>,
        moveLatitude: Double,
        moveLongitude: Double
    ) {
        val option=NaverMapOptions()
        option.compassEnabled(true)
        option.locationButtonEnabled(true)
        //지도의 특성 변경, 길게 눌렀을 때 실행방법
        mapView.getMapAsync {
            naverMap = it
            it.setOnSymbolClickListener {symbol->
                showTitle.text=symbol.caption
                val mark = putMarker(symbol.position.latitude,symbol.position.longitude)
                showPanel(symbol.position.latitude,symbol.position.longitude, false, null, mark, false)
                false
            }
            for (i in memoData) {
                initMarkerSetting(i)
            }
            if (checking == 1) {
                overlaySetting(latitude!!, longitude!!)
            }
            it.setOnMapLongClickListener { pointF, latLng ->
                val mark = putMarker(latLng.latitude, latLng.longitude)
                showPanel(latLng.latitude, latLng.longitude, false, null, mark, false)
                true
            }
            it.setOnMapClickListener { pointF, latLng ->
                if (slidingPanel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED)
                    slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                else if(slidingPanel.panelState==SlidingUpPanelLayout.PanelState.HIDDEN) {
                    if(mainBottomNavigation.visibility==View.VISIBLE){
                        toolbar.visibility=View.INVISIBLE
                        mainBottomNavigation.visibility=View.INVISIBLE
                        showTitle.visibility=View.INVISIBLE
                    }
                    else{
                        showTitle.visibility=View.VISIBLE
                        toolbar.visibility=View.VISIBLE
                        mainBottomNavigation.visibility=View.VISIBLE}
                }

            }
            it.setOnMapDoubleTapListener { pointF, latLng ->
                //                if(bottomLayout.visibility==View.GONE){
//                    bottomLayout.visibility=View.VISIBLE
//                    slidingPanel.panelState=SlidingUpPanelLayout.PanelState.HIDDEN}
//                else {
//                    bottomLayout.visibility = View.GONE
//                    slidingPanel.panelState=SlidingUpPanelLayout.PanelState.COLLAPSED
//                }

                true
            }
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
            it.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true)
            if (moveLatitude == 0.0) {
                moveCamera(init_latitude, init_longitude)
                putMarker(init_latitude, init_longitude)
            }
                else {
                moveCamera(moveLatitude, moveLongitude)
                putMarker(moveLatitude,moveLongitude)
            }
            }
        map_View.addView(mapView)
    }

    //마커 기본특성
    private fun markerProperty(marker: Marker, latLng: LatLng): Marker {
        marker.position = LatLng(latLng.latitude, latLng.longitude)
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.isCaptionPerspectiveEnabled = true
        marker.setOnClickListener {
            showPanel(latLng.latitude, latLng.longitude, false, null, marker, false)
            true
        }

        marker.icon = MarkerIcons.LIGHTBLUE
        return marker
    }

    private fun initMarkerSetting(data: MemoData) {
        var marker = Marker()

        with(marker) {
            position = LatLng(data.latitude, data.longitude)
            width = Marker.SIZE_AUTO
            height = Marker.SIZE_AUTO
            isCaptionPerspectiveEnabled = true
            setOnClickListener {
                showPanel(data.latitude, data.longitude, true, data, marker, data.memoOrCalendar)

                true
            }
            setCaptionAligns(Align.Top)
            val info = InfoWindow()
            if (data.memoOrCalendar) {
                info.position = this.position
                info.adapter = object : InfoWindow.DefaultTextAdapter(this@MainActivity) {
                    override fun getText(p0: InfoWindow): CharSequence {
                        return "${data.month + 1}월 ${data.day}일"
                    }
                }
                info.setOnClickListener {

                    showPanel(
                        data.latitude,
                        data.longitude,
                        true,
                        data,
                        marker,
                        data.memoOrCalendar,
                        info
                    )
                    true
                }
                info.tag=true
                markerlist.add(info)
                info.map = naverMap
            } else {

                info.position = this.position
                info.adapter = object : InfoWindow.DefaultViewAdapter(this@MainActivity) {
                    override fun getContentView(p0: InfoWindow): View {
                        val view = View.inflate(this@MainActivity, R.layout.show_picture_map, null)
                        if (data.picture != null) {
                            val picture =
                                BitmapFactory.decodeByteArray(data.picture, 0, data.picture?.size!!)

                            view.firstPictureSample.setImageBitmap(picture)
                        }
                        return view
                    }
                }
                info.setOnClickListener {
                    showPanel(
                        data.latitude,
                        data.longitude,
                        true,
                        data,
                        marker,
                        data.memoOrCalendar,
                        info
                    )
                    true
                }
                info.tag=false
                markerlist.add(info)
                info.map = naverMap
            }
        }

    }

    private var total = 0
    private fun putMarker(latitude: Double?, longitude: Double?): Marker {
        if (markerCount == 2) {
            if (total % 2 == 0) {
                uniqueMarker1?.map = null
                uniqueMarker1 = null
            } else {
                uniqueMarker2?.map = null
                uniqueMarker2 = null

            }
            total++
            markerCount--
        }
        var marker = Marker()
        marker = markerProperty(marker, LatLng(latitude!!, longitude!!))
        if (uniqueMarker1 == null) {
            marker.map = naverMap
            uniqueMarker1 = marker
            markerCount++
            return uniqueMarker1!!
        } else if (uniqueMarker2 == null) {
            marker.map = naverMap
            uniqueMarker2 = marker
            markerCount++
            return uniqueMarker2!!
        }
        return uniqueMarker1!!

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

    private fun requestLocation() {
        Snackbar.make(
            Main_drawer,
            "폰의 위치기능을 켜야 사용할 수 있습니다.",
            30000
        ).setAction("설정", View.OnClickListener {
            val goSetting = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(goSetting, 100)
        }).show()
    }

    private fun confirmButtonClick(saveMarker: Marker){
        titleText.isClickable = true
        timerImage.isClickable = true
        contentText.isClickable = true
        confirmButton.setOnClickListener {
            when {
                titleText.text.toString() == "" -> Toast.makeText(
                    this,
                    "제목을 입력해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                (memo.year == 0 && mode) -> Toast.makeText(
                    this,
                    "시간을 설정해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                memo.latitude == 0.0 -> Toast.makeText(
                    this,
                    "위치를 설정해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                mode&& cal.after(GregorianCalendar(memo.year,memo.month,memo.day,memo.hour,memo.minute))->
                    Toast.makeText(
                        this,
                        "${cal[Calendar.YEAR]-2000}.${cal.get(Calendar.MONTH).plus(1)}." +
                                "${cal.get(Calendar.DATE)} ${cal[Calendar.HOUR]}:${cal[Calendar.MINUTE]} 이후로 시간 설정을 해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                    with(memo) {
                        title = titleText.text.toString()
                        latitude = saveMarker.position.latitude
                        longitude = saveMarker.position.longitude
                        contents =contentText.text.toString()
                        memoOrCalendar = mode
                        location=showTitle.text.toString()
                        if (pictureFlag != null) {
                            val byteStream = ByteArrayOutputStream()
                            pictureFlag?.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                            picture = byteStream.toByteArray()
                            byteStream.close()
                        }
                    }
                    val tempMemo = MemoData().deepCopy(memo)
                    with(saveMarker) {
                        val info = InfoWindow()
                        if (mode) {
                            info.position = this.position
                            info.adapter =
                                object : InfoWindow.DefaultTextAdapter(this@MainActivity) {
                                    override fun getText(p0: InfoWindow): CharSequence {
                                        return "${memo.month + 1}월 ${memo.day}일"
                                    }
                                }
                            this.map = null
                            info.tag=true
                            markerlist.add(info)
                            info.map = naverMap
                            info.setOnClickListener {
                                showPanel(
                                    this.position.latitude,
                                    this.position.longitude,
                                    true,
                                    tempMemo,
                                    this,
                                    tempMemo.memoOrCalendar,
                                    info
                                )
                                true
                            }
                        } else {
                            info.position = this.position
                            info.adapter =
                                object : InfoWindow.DefaultViewAdapter(this@MainActivity) {
                                    override fun getContentView(p0: InfoWindow): View {
                                        val view = View.inflate(
                                            this@MainActivity,
                                            R.layout.show_picture_map,
                                            null
                                        )
                                        if (pictureFlag != null) view.firstPictureSample.setImageBitmap(
                                            pictureFlag
                                        )
                                        return view
                                    }
                                }
                            info.setOnClickListener {
                                showPanel(
                                    this.position.latitude,
                                    this.position.longitude,
                                    true,
                                    tempMemo,
                                    this,
                                    tempMemo.memoOrCalendar,
                                    info
                                )
                                true
                            }
                            info.tag=false
                            markerlist.add(info)
                            info.map = naverMap
                        }
                    }
                    uniqueMarker1 = null
                    uniqueMarker2 = null
                    markerCount = 0
                    realm?.executeTransaction {
                        it.copyToRealm(memo)
                    }
                    Toast.makeText(this, "Toyou 메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        if(memo.year==cal[Calendar.YEAR]&&memo.month==cal[Calendar.MONTH]&&memo.day==cal[Calendar.DATE]){
                            println("Checking")
                            val todayMemoData = MemoClass(realm!!).getDayMemoData(
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                                cal.get(Calendar.DATE)
                            )
                            val checkWord = if (todayMemoData.size == 0) "오늘의 일정은 없습니다." else ""
                            mainRecyclerView2.adapter = DailyListAdapter2(checkWord, todayMemoData) {
                                moveCamera(it.latitude, it.longitude)
                            }
                        }
                    memo = MemoData()
                }

            }
        }
        confirmButton.text = "저장"
    }
    private fun showPanel(
        latitude: Double,
        longitude: Double,
        touchFlag: Boolean,
        data: MemoData?,
        marker: Marker?,
        memoOrCalendar: Boolean,
        info: InfoWindow? = null
    ) {
        bottom_slide.currentItem=0
        toolbar.visibility=View.VISIBLE
        showTitle.visibility=View.VISIBLE
        cal=GregorianCalendar()
        slidingPanel.panelState=SlidingUpPanelLayout.PanelState.EXPANDED
        //새로운 패널이 열릴 때마다 사진 초기화
        pictureFlag = null

        galleryImage.setImageResource(R.drawable.photo)
        if (touchFlag) {
            //메모인지 일정인지 확인
            if (memoOrCalendar) {
                timerImage.visibility=View.VISIBLE
                galleryImage.visibility=View.GONE
                if (data != null) {
                    indicateText.text =
                        "${data.year}년 ${data.month + 1}월${data.day}일 ${data.hour}시${data.minute}분"
                }
            } else {
                timerImage.visibility=View.GONE
                galleryImage.visibility=View.VISIBLE
                indicateText.text=""
//                val dialogIntent=Intent(this,DialogView::class.java)
//                dialogIntent.putExtra("memoData",data)
//                startActivity(dialogIntent)
            }
            memoButton.visibility = View.INVISIBLE
            calendar.visibility = View.INVISIBLE
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                realm?.executeTransaction {
                    if (data != null) {
                        val removeData =
                            it.where(MemoData::class.java).equalTo("id", data.id).findFirst()
                        removeData?.deleteFromRealm()
                        Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
                if (info == null)
                    marker?.map = null
                else {
                    info.map = null
                     for(i in 0 until markerlist.size){
                         if(markerlist[i]==info){
                             markerlist.removeAt(i)
                             break
                         }
                     }
                }
                    titleText.setText("")
                    contentText.setText("")
                    indicateText.text = ""
                    deleteButton.visibility = View.INVISIBLE

                slidingPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            showTitle.text=data?.location
                titleText.setText(data?.title.toString())
                contentText.setText(data?.contents.toString())
                titleText.isClickable = false
                timerImage.isClickable = false
                contentText.isClickable = false
                confirmButton.setOnClickListener {
                    reviseMarkerInfo(data!!, marker!!)
                }

            confirmButton.text = "수정"
        } else {
            timerImage.visibility=View.VISIBLE
            galleryImage.visibility=View.GONE
            memoButton.visibility = View.VISIBLE
            calendar.visibility = View.VISIBLE
            calendar.isSelected=true
            memoButton.isSelected=false
            deleteButton.visibility = View.INVISIBLE
            titleText.setText("")
            contentText.setText("")
            indicateText.text = "시간 설정->"
            confirmButtonClick(marker!!)
            memo.latitude = latitude
            memo.longitude = longitude
            mode=true
        }

        endButton.setOnClickListener {
            endLocation = LatLng(latitude, longitude)
            viewDestinationOnMap(startLocation, "목적지점이 지정되었습니다.")
            if (showTitle.text.toString() == "장소 및 위치 검색하기")
                showTitle.text = "출발지점을 지정하세요"
        }
        startButton.setOnClickListener {
            startLocation = LatLng(latitude, longitude)
            showTitle.text = DirectionFinder().convertToAddress(latitude, longitude)
            viewDestinationOnMap(endLocation, "출발지점이 지정되었습니다.")
        }
        moveCamera(latitude, longitude)
    }

    private fun viewDestinationOnMap(which: LatLng?, text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        startFlag = 1
        if (which != null) {
            goToTransferActivity()
        }
    }

    private fun goToTransferActivity() {
        val intentToList = Intent(this, ChooseTransferList::class.java)
        intentToList.putExtra("start", startLocation)
        intentToList.putExtra("destination", endLocation)
        startActivityForResult(intentToList, 5)
    }

    private fun reviseMarkerInfo(data: MemoData, marker: Marker) {
        realm?.executeTransaction {
            data.title = titleText.text.toString()
            data.contents =contentText.text.toString()
            data.month = memo.month
            data.day = memo.day
            data.hour = memo.hour
            data.minute = memo.minute
            data.year = memo.year
        }
        marker.captionText = data.title
        Toast.makeText(this, "메모 내용이 수정되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission(): Boolean {
        val finePermission =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        val coarsePermission =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        return finePermission && coarsePermission
    }

    private fun checkFileReadPermission(): Boolean {
        val readCheck = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val writeCheck = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return readCheck && writeCheck
    }
    inner class MainPagerAdapter : RecyclerView.Adapter<DailyListViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return if (position == 0) 1
            else 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
            return if (viewType == 0) DailyListViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.main_first_indicator_show,
                    parent,
                    false
                )
            )
            else DailyListViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.main_second_indicator_show,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = 2


        override fun onBindViewHolder(holder: DailyListViewHolder, position: Int){//첫번째 메인 화면
            val item=getItemViewType(position)
            if (item == 0) {
                holder.itemView.rightLayout.setOnClickListener{
                    bottom_slide.setCurrentItem(1,true)
                }
                holder.itemView.todayList.text =
                    "오늘의 일정(${cal.get(Calendar.YEAR)}.${cal.get(Calendar.MONTH) + 1}.${cal.get(
                        Calendar.DATE
                    )})"
                holder.itemView.calendarLayout2.setOnClickListener {
                    val intent = Intent(this@MainActivity, CalendarActivity::class.java)
                    startActivityForResult(intent, 30)
                }
                mainRecyclerView2=holder.itemView.mainRecyclerView2
                mainRecyclerView2.layoutManager = LinearLayoutManager(this@MainActivity)
                //오늘 메모장 adapter
                val todayMemoData = MemoClass(realm!!).getDayMemoData(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DATE)
                )
                val checkWord = if (todayMemoData.size == 0) "오늘의 일정은 없습니다." else ""
                mainRecyclerView2.adapter = DailyListAdapter2(checkWord, todayMemoData) {
                    moveCamera(it.latitude, it.longitude)
                }
            }
            else {
                with(holder.itemView){
                    this.calendar.isSelected=true
                    this.galleryImage.visibility=View.GONE
                    leftLayout.setOnClickListener {
                        bottom_slide.setCurrentItem(0,true)
                    }
                    deleteButton.visibility = View.GONE
                    calendar.setOnClickListener {
                        calendar.isSelected=true
                        memoButton.isSelected=false
                        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                        this.timerImage.visibility=View.VISIBLE
                        this.galleryImage.visibility=View.GONE
                        indicateText.text="시간 설정->"
                        memo.year=0
                        mode = true
                    }
                    memoButton.setOnClickListener {
                        memoButton.isSelected=true
                        calendar.isSelected=false
                        this.galleryImage.visibility=View.VISIBLE
                        this.timerImage.visibility=View.GONE
                        indicateText.text="지도에 표시할 사진을 설정(선택)"
                        mode = false
                        pictureFlag=null
                        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }
                    galleryImage.setOnClickListener {
                        if (!checkFileReadPermission()) {
                            Toast.makeText(this@MainActivity, "앨범 권한이 필요합니다", Toast.LENGTH_LONG).show()
                            ActivityCompat.requestPermissions(
                                this@MainActivity, arrayOf(
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ), 150
                            )
                        } else {
                            val galleryIntent = Intent()
                            galleryIntent.type = "image/*"
                            galleryIntent.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(galleryIntent, 150)
                        }
                    }

                }

                galleryImage=holder.itemView.galleryImage
                confirmButton=holder.itemView.confirmButton
                deleteButton=holder.itemView.deleteButton
                timerImage=holder.itemView.timerImage
                calendar=holder.itemView.calendar
                memoButton=holder.itemView.memoButton
                titleText=holder.itemView.titleText
                contentText=holder.itemView.dialogContent
                startButton=holder.itemView.startButton
                endButton=holder.itemView.endButton
                indicateText=holder.itemView.indicateText
                holder.itemView.timerImage.setOnClickListener {
                    val date = DatePickerDialog(
                        this@MainActivity,
                        DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                            memo.year = year
                            memo.month = month
                            memo.day = dayOfMonth
                                indicateText.text =
                                "${memo.year}년 ${memo.month + 1}월${memo.day}일 ${memo.hour}시 ${memo.minute}분"
                        }, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE)
                    )
                    date.setTitle("Choose Time")
                    date.show()
                    TimePickerDialog(this@MainActivity, TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                        memo.hour = hour
                        memo.minute = minute

                    }, cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE), false).show()
                }

            }

    }


}}

@RealmModule(classes = [MemoData::class])
class RealmModuleMemo{}




