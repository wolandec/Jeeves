package wolandec.jeeves

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


class SettingsActivity : AppCompatActivity() {

    val LOG_TAG = this::class.java.simpleName
    var sharedPrefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPref?.getBoolean("enable_jeeves", false) == true) {
            registerBroadcastService()
        }

        sharedPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sP, key ->
            if (key == "enable_jeeves") {
                if (sharedPref?.getBoolean("enable_jeeves", false) == true)
                    registerBroadcastService()
            }
        }
        sharedPref?.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);

    }

    fun checkPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.RECEIVE_SMS,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_COARSE_LOCATION),
                        1)
        }
        checkDoNotDisturb()
    }

    @SuppressLint("NewApi")
    private fun checkDoNotDisturb() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        val n = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (n.isNotificationPolicyAccessGranted) {
        } else {
            // Ask the user to grant access
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun registerBroadcastService() {
        val i = Intent("wolandec.jeeves.JeevesService")
        i.setClass(this, JeevesService::class.java!!)
        try {
            this!!.startService(i)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

}
