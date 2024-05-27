package com.example.projecta2.View

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.R
import com.example.projecta2.adapter.FitnessCenterAdapter
import com.example.projecta2.model.FitnessCenter
import com.example.projecta2.util.RetrofitInstance
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.projecta2.databinding.ActivityMapBinding
import kotlinx.coroutines.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var fitnessCenterAdapter: FitnessCenterAdapter
    private var userInfo: UserInfo? = null

    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton
    private lateinit var homeButton: LinearLayout
    private lateinit var myPageButton: LinearLayout

    private val REQUEST_PERMISSION_LOCATION = 1000

    private lateinit var binding: ActivityMapBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        homeButton = binding.mapToHome
        myPageButton = binding.mapToMyPage



        homeButton.setOnClickListener {
            startActivity(
                Intent(
                    this@MapActivity,
                    HomeActivity::class.java
                )
            )
        }
        myPageButton.setOnClickListener {
            startActivity(
                Intent(
                    this@MapActivity,
                    MyPageActivity::class.java
                )
            )
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getUserLocation()

        map.setOnMapClickListener(this)
    }

    override fun onMapClick(latLng: LatLng) {
        addMarkerAt(latLng)
    }

    private fun addMarkerAt(latLng: LatLng) {
        // 기존 마커 지우기
        map.clear()
        // 클릭한 위치에 마커 추가
        map.addMarker(MarkerOptions().position(latLng).title("Clicked Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

        // 클릭한 위치에서 피트니스 센터 다시 가져오기
        CoroutineScope(Dispatchers.Main).launch {

            fetchFitnessCenters(latLng)

        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_PERMISSION_LOCATION
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f))
                val userMarkerOptions = MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .alpha(0.7f)
                map.addMarker(userMarkerOptions)

                CoroutineScope(Dispatchers.Main).launch {
                    fetchFitnessCenters(userLocation)
                }
            }
        }
    }

    private suspend fun fetchFitnessCenters(userLocation: LatLng) {
        val gymService = RetrofitInstance.gymService
        try {
            val response = withContext(Dispatchers.IO) {
                gymService.getGymList().execute()
            }
            if (response.isSuccessful) {
                response.body()?.let { list ->
                    val geocoder = Geocoder(applicationContext)
                    val fitnessCentersWithLatLng = convertAddressToLatLng(list, geocoder)
                    val fitnessCentersNearby =
                        getFitnessCentersNearby(userLocation, fitnessCentersWithLatLng)
                    calculateAndSetDistance(userLocation, fitnessCentersNearby)
                    showFitnessCentersOnMapAndList(userLocation, fitnessCentersNearby)
                } ?: run {
                    Log.e("Response Error", "Received null center list")
                }
            } else {
                Log.e("Response Error", "Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("Request Failed", "Error: ${e.message}", e)
        }
    }

    private fun showFitnessCentersOnMapAndList(
        userLocation: LatLng,
        fitnessCentersNearby: List<FitnessCenter>
    ) {
        // 지도에 피트니스 센터 마커 표시
        for (center in fitnessCentersNearby) {
            val location = LatLng(center.latitude, center.longitude)
            map.addMarker(MarkerOptions().position(location).title(center.name))
        }
        // 리사이클러 뷰 갱신
        updateRecyclerView(fitnessCentersNearby)
    }

    private fun updateRecyclerView(fitnessCentersNearby: List<FitnessCenter>) {
        if (fitnessCentersNearby.isNotEmpty()) {
            setupRecyclerView(fitnessCentersNearby)
        } else {
            // 센터가 없는 경우 빈 목록을 전달하여 리사이클러 뷰를 갱신
            setupRecyclerView(emptyList())
        }
    }


    private fun setupRecyclerView(fitnessCentersNearby: List<FitnessCenter>) {
        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        if (fitnessCentersNearby.isNotEmpty()) {
            fitnessCenterAdapter = FitnessCenterAdapter(fitnessCentersNearby, userInfo)
            recyclerView.adapter = fitnessCenterAdapter
        } else {
            // 센터가 없는 경우, 빈 목록을 생성하여 리사이클러 뷰를 초기화
            fitnessCenterAdapter = FitnessCenterAdapter(emptyList(), userInfo)
            recyclerView.adapter = fitnessCenterAdapter
        }
    }

    private fun convertAddressToLatLng(
        centerList: List<FitnessCenter>,
        geocoder: Geocoder
    ): List<FitnessCenter> {
        val fitnessCentersWithLatLng = mutableListOf<FitnessCenter>()
        for (center in centerList) {
            val addresses = geocoder.getFromLocationName(center.address, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val lat = addresses[0].latitude
                    val lng = addresses[0].longitude
                    val centerWithLatLng = center.copy(latitude = lat, longitude = lng)
                    fitnessCentersWithLatLng.add(centerWithLatLng)
                }
            }
        }
        return fitnessCentersWithLatLng
    }

    private fun getFitnessCentersNearby(
        userLocation: LatLng,
        fitnessCenters: List<FitnessCenter>
    ): List<FitnessCenter> {
        val fitnessCentersNearby = mutableListOf<FitnessCenter>()
        for (center in fitnessCenters) {
            val location = LatLng(center.latitude, center.longitude)
            val distance = distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                location.latitude,
                location.longitude
            )
            if (distance <= 1000) {
                fitnessCentersNearby.add(center)
            }
        }
        return fitnessCentersNearby
    }

    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }

    private fun calculateAndSetDistance(userLocation: LatLng, fitnessCenters: List<FitnessCenter>) {
        for (center in fitnessCenters) {
            val location = LatLng(center.latitude, center.longitude)
            val distance = distanceBetween(userLocation.latitude, userLocation.longitude, location.latitude, location.longitude)
            center.distance = distance // 거리 설정
        }
    }
}
