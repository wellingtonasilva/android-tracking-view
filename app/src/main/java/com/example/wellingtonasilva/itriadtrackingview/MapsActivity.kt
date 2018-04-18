package com.example.wellingtonasilva.itriadtrackingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.wellingtonasilva.itriadtrackingview.util.AnimatorPosition
import com.example.wellingtonasilva.itriadtrackingview.util.Utils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var handler: Handler
    private val coordenadas = ConcurrentLinkedQueue<AnimatorPosition>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        handler = Handler()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap)
    {
        mMap = googleMap

        val torquato = LatLng(-3.018459, -60.027603)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(torquato))

        //Zoom
        addMarkWithZoom(-3.018459, -60.027603, 15f)

        val observable: Observable<List<LatLng>> = Observable.create { e -> e.onNext(getCoordenates()) }
        observable
                .observeOn(AndroidSchedulers.mainThread())
                //Add Bus Stop Marker
                .flatMap { t: List<LatLng> -> Observable.create(ObservableOnSubscribe<List<LatLng>> { e -> e.onNext(addMarkBusStop(t)) })}
                //Create Animation List
                .flatMap { t: List<LatLng> -> Observable.create(ObservableOnSubscribe<ConcurrentLinkedQueue<AnimatorPosition>>{e ->
                    addAninationIntoQueue(getAnimation(t, R.drawable.ic_bus, "Roa 1"))
                    e.onNext(coordenadas)
                })}
                .subscribeOn(Schedulers.io())
                .subscribe { t: ConcurrentLinkedQueue<AnimatorPosition>? ->  startAnimation()}
    }

    fun getCoordenates(): List<LatLng>
    {
        val json = Utils.loadJSONFromAsset(applicationContext, "coordenadas.json")
        return Gson().fromJson<List<LatLng>>(json, object : TypeToken<List<LatLng>>() {}.type)
    }

    fun addMarkWithZoom(latitude: Double, longitude: Double, zoom: Float)
    {
        val latLong = LatLng(latitude, longitude)
        val cameraPosition = CameraPosition.Builder()
                .target(latLong)
                .zoom(zoom)
                .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun addMarkBusStop(list: List<LatLng>): List<LatLng>
    {
        try {
            var i = 0
            list.forEach { item -> i++
                if (i == 3)
                {
                    i = 0
                    val marker = mMap.addMarker(MarkerOptions()
                            .position(LatLng(item.latitude, item.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop))
                            .title(""))
                }
            }
        } catch (e: Exception) {
            Log.d("addMarkBusStop", e.message)
        }

        return list
    }

    fun getAnimation(list: List<LatLng>, icon: Int, rota: String): AnimatorPosition
    {
        return AnimatorPosition(mMap, list, icon, applicationContext, rota, handler)
    }

    fun addAninationIntoQueue(animatorPosition: AnimatorPosition) {
        coordenadas.add(animatorPosition)
    }

    fun startAnimation()
    {
        coordenadas.forEach { item -> item.initialize(true)
            handler.postDelayed(item, 1000)
        }
    }
}
