package com.example.officetracker.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.example.officetracker.R
import com.example.officetracker.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private var user: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        user =
            if (mAuth.currentUser?.displayName != null) mAuth.currentUser?.displayName.toString() else mAuth.currentUser?.email
        setNavigationDrawer()
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
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
            date.text = getDate(currentDate)
        }

        if (LocalTime.now().isAfter(LocalTime.parse("09:00"))) {
            binding.punchButton.visibility = View.VISIBLE
        } else {
            binding.checkInText.visibility = View.VISIBLE
        }
        binding.signOutBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.do_you_want_to_log_out).setPositiveButton(
                R.string.ok
            )
            { _, _ ->
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

    private fun getTime(dateStr: String): String {
        var time = ""
        try {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
            val mDate = formatter.parse(dateStr)
            time = Time(mDate!!.time).toString()
        } catch (e: Exception) {
            Log.e("mTime", e.toString())
        }
        return time
    }

    private fun getDate(dateStr: String): String {
        var mDate = ""
        try {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            mDate = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(formatter.parse(dateStr)!!)
        } catch (e: Exception) {
            Log.e("mDate", e.toString())
        }
        return mDate
    }
}

