package com.example.wellingtonasilva.itriadtrackingview.util

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import java.util.*

class AnimatorPosition: Runnable
{
    private val ANIMATE_SPEEED = 1200
    private val ANIMATE_SPEEED_TURN = 1000
    private val BEARING_OFFSET = 20
    private val interpolator = LinearInterpolator()
    //Actual Position
    internal var currentIndex = 0
    //
    internal var tilt = 90f
    //Zoom Default
    internal var zoom = 15.5f
    internal var upward = true
    internal var start = SystemClock.uptimeMillis()
    //Initial Coordenate
    internal var endLatLng: LatLng? = null
    //End Coordenate
    internal var beginLatLng: LatLng? = null
    //Show Polyline when move Marker
    internal var showPolyline = false
    private val googleMap: GoogleMap
    //Marker List
    private val markers = ArrayList<Marker>()
    //Actual Selected Marker
    private var selectedMarker: Marker? = null
    //Color (Type) Icon to show on Map
    private val iconMarker: Int
    //Polyline's color
    private val polylineColor: Int = 0
    //Coordenate to moving the marker
    private var latLng: List<LatLng>
    private var trackingMarker: Marker? = null
    private val mHandler: Handler
    private val rota: String
    private val context: Context
    private val polyLine: Polyline? = null
    private val rectOptions = PolylineOptions()

    constructor(googleMap: GoogleMap, latLng: List<LatLng>, iconMarker: Int,
                context: Context, rota: String, handler: Handler)
    {
        this.googleMap = googleMap
        this.mHandler = handler
        this.iconMarker = iconMarker
        this.latLng = latLng
        this.context = context
        this.rota = rota

        resetMarkers()
        adicionarCoordenada(latLng)
    }

    private fun adicionarCoordenada(latLng: List<LatLng>)
    {
        //Adicionar apenas o marcador inicial
        if (latLng.size > 0) {
            addMarkerToMap(latLng[0])
        }
    }

    fun reset()
    {
        resetMarkers()
        start = SystemClock.uptimeMillis()
        currentIndex = 0
        endLatLng = getEndLatLng()
        beginLatLng = getBeginLatLng()
        if (markers != null && markers.size == 0) {
            adicionarCoordenada(latLng)
        }
    }

    fun stop()
    {
        trackingMarker?.remove()
        //Remover o primeiro Marcador
        if (markers != null && markers.size > 0) {
            markers[0].remove()
        }
        //mHandler.removeCallbacks(animator);
    }

    fun initialize(showPolyLine: Boolean): Boolean
    {
        reset()
        this.showPolyline = showPolyLine
        highLightMarker(0)

        if (showPolyLine) {
            // polyLine = initializePolyLine();
        }

        val markerPos = latLng[0]
        val secondPos = latLng[1]

        setupCameraPositionForMovement(markerPos, secondPos)

        return true
    }

    private fun setupCameraPositionForMovement(markerPos: LatLng, secondPos: LatLng)
    {

        val bearing = bearingBetweenLatLngs(markerPos, secondPos)
        trackingMarker = googleMap.addMarker(MarkerOptions()
                .position(markerPos)
                .icon(BitmapDescriptorFactory.fromResource(iconMarker))
                .title(rota))
    }

    private fun initializePolyLine(): Polyline?
    {
        //polyLinePoints = new ArrayList<LatLng>();

        //  rectOptions.add(markers.get(0).getPosition()).color(Color.YELLOW);
        //  return googleMap.addPolyline(rectOptions);
        return null
    }

    /**
     * Add the marker to the polyline.
     */
    private fun updatePolyLine(latLng: LatLng)
    {
        /*
        List<LatLng> points = polyLine.getPoints();
        points.add(latLng);
        polyLine.setPoints(points);
        */

        if (currentIndex > 0) {
            val polyOptions = PolylineOptions()
                    .add(this.latLng[currentIndex - 1], latLng)
                    .color(Color.YELLOW)
            //.width();

            // Draw polyline for segment
            googleMap.addPolyline(polyOptions)
        }

    }

    fun stopAnimation() {
        stop()
    }

    fun startAnimation(showPolyLine: Boolean) {
        if (markers.size > 2) {
            initialize(showPolyLine)
        }
    }

    override fun run()
    {
        val elapsed = SystemClock.uptimeMillis() - start
        //double t = interpolator.getInterpolation((float)elapsed/ANIMATE_SPEEED);
        val t = 2.0

        // double lat = t * endLatLng.latitude + (1-t) * beginLatLng.latitude;
        //double lng = t * endLatLng.longitude + (1-t) * beginLatLng.longitude;

        markers[0].remove()
        val lat = endLatLng?.latitude
        val lng = endLatLng?.longitude
        val newPosition = LatLng(lat!!, lng!!)

        // trackingMarker.setPosition(null);
        trackingMarker?.setPosition(newPosition)

        if (showPolyline) {
            updatePolyLine(newPosition)
        }

        // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
        //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
        //navigateToPoint(newPosition,false);

        if (t < 1) {
            mHandler.postDelayed(this, 2000)
        } else
        {
            //"Move to next marker.... current = " + currentIndex + " and size = " + markers.size
            // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
            //if (currentIndex<markers.size()-2) {
            if (currentIndex < latLng.size - 2) {
                currentIndex++
                endLatLng = getEndLatLng()
                beginLatLng = getBeginLatLng()

                start = SystemClock.uptimeMillis()

                val begin = getBeginLatLng()
                val end = getEndLatLng()

                val bearingL = bearingBetweenLatLngs(begin, end)

                //highLightMarker(currentIndex);

                // start = SystemClock.uptimeMillis();
                mHandler.postDelayed(this, 2000)

            } else {
                currentIndex++
                //Adiconar o Marker Final
                addMarkerToMap(latLng[currentIndex])
                highLightMarker(markers.size - 1)
                stopAnimation()
            }

        }
    }

    private fun getEndLatLng(): LatLng
    {
        //return markers.get(currentIndex+1).getPosition();
        return latLng[currentIndex + 1]
    }

    private fun getBeginLatLng(): LatLng {
        //return markers.get(currentIndex).getPosition();
        return latLng[currentIndex]
    }

    private fun adjustCameraPosition() {
        if (upward) {
            if (tilt < 90) {
                tilt++
                zoom -= 0.01f
            } else {
                upward = false
            }
        } else {
            if (tilt > 0) {
                tilt--
                zoom += 0.01f
            } else {
                upward = true
            }
        }
    }

    /**
     * Adds a marker to the map.
     */
    fun addMarkerToMap(latLng: LatLng) {
        val marker = googleMap.addMarker(MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(iconMarker))
                .title(this.rota))
        markers.add(marker)
    }

    /**
     * Clears all markers from the map.
     */
    fun clearMarkers() {
        googleMap.clear()
        markers.clear()
    }

    /**
     * Remove the currently selected marker.
     */
    fun removeSelectedMarker() {
        this.markers.remove(this.selectedMarker!!)
        this.selectedMarker?.remove()
    }

    /**
     * Highlight the marker by index.
     */
    private fun highLightMarker(index: Int) {
        highLightMarker(markers[index])
    }

    /**
     * Highlight the marker by marker.
     */
    private fun highLightMarker(marker: Marker)
    {
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.setIcon(BitmapDescriptorFactory.fromResource(iconMarker))
        // marker.showInfoWindow();

        //Utils.bounceMarker(googleMap, marker);
        this.selectedMarker = marker
    }

    private fun resetMarkers() {
        for (marker in this.markers) {
            //marker.setIcon(BitmapDescriptorFactory.fromResource(iconMarker))
        }
    }

    private fun convertLatLngToLocation(latLng: LatLng): Location
    {
        val loc = Location("someLoc")
        loc.latitude = latLng.latitude
        loc.longitude = latLng.longitude

        return loc
    }

    private fun bearingBetweenLatLngs(begin: LatLng, end: LatLng): Float
    {
        val beginL = convertLatLngToLocation(begin)
        val endL = convertLatLngToLocation(end)

        return beginL.bearingTo(endL)
    }
}
