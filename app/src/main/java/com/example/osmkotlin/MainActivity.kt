package com.example.osmkotlin

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem

class MainActivity : AppCompatActivity() {
     private val PERMISSIONREQUESTCODE = 101
     var appPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
        )
     var mapView : MapView?=null
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()

        initMap()
    }
    // method for initializing map
    private fun initMap(){
        mapView = findViewById(R.id.map)

        mapView?.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)

        mapView?.setMultiTouchControls(true)


        mapView?.controller?.setZoom(15)
        val gPt = GeoPoint(28.5747334, 77.3561638)
        val gPt2 = GeoPoint(28.5356329, 77.3910727)
        mapView?.controller?.setCenter(gPt)




        /*place icon on map*/
        val items = ArrayList<OverlayItem>()
        items.add(OverlayItem("Current Location", "Current Location", gPt))
        items.add(OverlayItem("Destination", "Destination Location", gPt2))

        val mOverlay = ItemizedOverlayWithFocus<OverlayItem>(items,
                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                        //do something
                        Log.d("TAG", "onItemSingleTapUp()")
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                        Log.d("TAG", "onItemLongPress()")
                        return false
                    }
                }, this)
        mOverlay.setFocusItemsOnTap(true)
        mapView?.overlays?.add(mOverlay)
    }
    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView?.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mapView?.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }
    /*method for checking and giving run time permissions*/
    private fun checkAndRequestPermissions():Boolean{
        // check which permissions are not granted

        var listPermissionsNeeded = arrayListOf<String>()

        for (perm in appPermissions){
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm)
            }

        }
        // ask for non granted permissions
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                PERMISSIONREQUESTCODE
            )
            return false
        }

        return  true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONREQUESTCODE) {
            var permissionResults = HashMap<String, Int>()
            var deniedCount =0
            for(i in grantResults.indices){
                // add only permissions which are denied

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i])
                    deniedCount++
                }

            }

            // check if all permissions are granted

            if (deniedCount == 0) {

            } else {
                for (entry in permissionResults.entries){
                    var perName = entry.key
                    var perResult = entry.value

                    // permission is denied (this is the first time, when "never ask again" is not checked)
                    // so ask again explaining the usage of permission
                    // shouldShowRequestPermissionRationale will return true


                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, perName)) {
                        // show Dialog of explanation
                        showDialog(
                            "",
                            "This app needs Location and Camera permissions to work without problems.",
                            "Yes, Grant permissions",
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                checkAndRequestPermissions()
                            },
                            "No, Exit app",
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            },
                            false
                        )

                    }
                    // permission is denied and (never ask again is checked)
                    // shouldShowRequestPermissionRationale will return false
                    else{

                        // Ask user to  go to settings and manually allow permissions
                        showDialog(
                            "",
                            "You have denied some permissions. Allow all permissions at [Settings] > [Permissions",
                            "Go to Settings",
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                var intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                                        "package",
                                        packageName,
                                        null
                                    )
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            },
                            "No, Exit app",
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                finish()
                            },
                            false
                        )
                    }
                }
            }

        }
    }


    /*method for showing permission dialog*/
    private fun showDialog(
        title: String,
        msg: String,
        positiveLabel: String,
        positiveOnClick: DialogInterface.OnClickListener,
        negativeLabel: String,
        negativeOnClick: DialogInterface.OnClickListener,
        isCancelable: Boolean
    ):AlertDialog{

        var builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setCancelable(isCancelable)
        builder.setPositiveButton(positiveLabel, positiveOnClick)
        builder.setNegativeButton(negativeLabel, negativeOnClick)
        var alertDialog = builder.create()
        alertDialog.show()
        return alertDialog
    }
}