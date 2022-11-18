package com.example.officetracker.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.example.officetracker.R
import com.example.officetracker.databinding.ActivityMainBinding
import com.example.officetracker.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private var user: String? = null
    private lateinit var locationDialogBuilder: android.app.AlertDialog.Builder
    private lateinit var locationDialog: android.app.AlertDialog
    private lateinit var gpsBroadcastReceiver: GPSBroadcastReceiver
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionRequiredCode = 1000
    private val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        user =
            if (mAuth.currentUser?.displayName != null) mAuth.currentUser?.displayName.toString() else mAuth.currentUser?.email
        setNavigationDrawer()
        binding.apply {
            toggle = ActionBarDrawerToggle(
                this@MainActivity,
                drawerLayout,
                R.string.open,
                R.string.close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            name.text = user
            date.text = getDate()
            inTime.text=MyPreference.readPrefString(this@MainActivity,Constants.CHECKED_IN_TIME)
            endTime.text=MyPreference.readPrefString(this@MainActivity,Constants.CHECKED_OUT_TIME)
        }

        if(MyPreference.readPrefBool(this,Constants.IS_CHECKED_IN)){
            binding.punchButton.text=getString(R.string.check_out)
        }
        //gps enabled or not
        gpsBroadcastReceiver = GPSBroadcastReceiver()
        gpsBroadcastReceiver.setLocationListener(this)
        setupLocationDialog()
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (this::locationDialog.isInitialized) {
                locationDialog.dismiss()
            }
        } else {
            if (this::locationDialog.isInitialized) {
                locationDialog.show()
            }
        }
        showPunchIn()
        //punch in time
        handler.postDelayed(object : Runnable {
            override fun run() {
                showPunchIn()
                handler.postDelayed(this, 1000 * 10 * 1)
            }
        }, 1000 * 10 * 1)

        binding.punchButton.setOnClickListener {
            if(MyPreference.readPrefBool(this,Constants.IS_CHECKED_IN)) {
                MyPreference.writePrefBool(this, Constants.IS_CHECKED_IN, false)
                MyPreference.writePrefString(this, Constants.CHECKED_OUT_TIME, getTime())
                binding.punchButton.text = getString(R.string.check_in)
                binding.endTime.text = getTime()
            }else{
                MyPreference.writePrefBool(this, Constants.IS_CHECKED_IN, true)
                MyPreference.writePrefString(this, Constants.CHECKED_IN_TIME, getTime())
                MyPreference.writePrefString(this,Constants.CHECKED_IN_DATE,getDate())
                binding.punchButton.text = getString(R.string.check_out)
                binding.inTime.text = getTime()
            }
        }
        binding.signOutBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.do_you_want_to_log_out).setPositiveButton(
                R.string.ok
            )
            { _, _ ->
                MyPreference.clear(this)
                mAuth.signOut()
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
            }
            dialog.setNegativeButton(R.string.no) { d, _ ->
                d.dismiss()
            }
            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setNavigationDrawer() {
        binding.navView.getHeaderView(0).findViewById<AppCompatTextView>(R.id.header_title).text =
            "Welcome\n${user}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) true
        return super.onOptionsItemSelected(item)
    }

    private fun setupLocationDialog() {
        locationDialogBuilder = android.app.AlertDialog.Builder(this, R.style.DialogTheme)
        locationDialogBuilder.setTitle("Location not available!")
        locationDialogBuilder.setMessage("Please enable Location/GPS to continue using application")
        locationDialogBuilder.setCancelable(false)
        locationDialog = locationDialogBuilder.create()
    }

    private fun getTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        var time = ""
        try {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
            val mDate = formatter.parse(currentDate)
            time = Time(mDate!!.time).toString()
        } catch (e: Exception) {
            Log.e("mTime", e.toString())
        }
        return time
    }

    private fun getDate(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        var mDate = ""
        try {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            mDate = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(formatter.parse(currentDate)!!)
        } catch (e: Exception) {
            Log.e("mDate", e.toString())
        }
        return mDate
    }

    override fun onStart() {
        super.onStart()
        if (this::gpsBroadcastReceiver.isInitialized) {
            registerReceiver(
                gpsBroadcastReceiver,
                IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
        }
    }

    override fun onLocationChanged(isLocationEnable: Boolean) {
        if (!isLocationEnable) {
            if (this::locationDialog.isInitialized) {
                locationDialog.show()
            }
        } else {
            if (this::locationDialog.isInitialized) {
                locationDialog.dismiss()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPunchIn(){
        if (LocalTime.now().isAfter(LocalTime.parse("08:59"))) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            binding.checkInText.visibility = View.INVISIBLE
        } else {
            binding.checkInText.visibility = View.VISIBLE
        }

    }
    //fetching current location
    @SuppressLint("MissingPermission")
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.please_enable_location))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.setTitle(getString(R.string.location))
                alert.show()
            }
        }

    private fun getCurrentLocation() {
        PermissionUtils.requestAccessFineLocationPermission(
            this,
            locationPermissionRequiredCode
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpCurrentLocationListener()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    locationPermissionRequiredCode
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpCurrentLocationListener() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                try {
                    val loc1 = Location("")
                    loc1.latitude = location.latitude
                    loc1.longitude = location.longitude
                    val loc2 = Location("")
                    loc2.longitude = 77.0406
                    loc2.latitude = 28.4392
                    val distanceInMeters: Float = loc1.distanceTo(loc2)
                    if (distanceInMeters <= 5.0) {
                        binding.punchButton.visibility = View.VISIBLE
                    } else {
                        binding.checkInText.text = getString(R.string.not_in_range)
                        binding.checkInText.visibility = View.VISIBLE
                    }
                    Log.d("distanceInMeters", distanceInMeters.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, getString(R.string.failed_on_getting_current_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

