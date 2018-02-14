package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.*
import android.preference.PreferenceManager
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class JeevesService() : Service(), LocationListener {

    val LOG_TAG = this::class.java.simpleName
    private var needLocationSMSToSend: Boolean = false
    private var currentSMSMessageEvent: SMSMessageEvent? = null
    private var locationManager: LocationManager? = null
    var sharedPref: SharedPreferences? = null
    var sharedPrefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    lateinit var location: Location
    var brReceiver: JeevesReceiver = JeevesReceiver()

    constructor(parcel: Parcel) : this() {
        location = parcel.readParcelable(Location::class.java.classLoader)
    }

    private fun registerIntentReceiver() {
        registerReceiver(brReceiver,
                IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        registerReceiver(brReceiver,
                IntentFilter("android.intent.action.MY_PACKAGE_REPLACED"))
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        Toast.makeText(applicationContext, getString(R.string.on_boot_string), Toast.LENGTH_SHORT).show()
        registerIntentReceiver()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sP, key ->
            if (key == "enable_jeeves") {
                if (sharedPref?.getBoolean("enable_jeeves", false) == false)
                    this.stopSelf()
            }
        }
        sharedPref?.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);
    }

    override fun onDestroy() {
        if (sharedPref?.getBoolean("enable_jeeves", false) == false) {
            sharedPref?.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener)
            unregisterReceiver(brReceiver)
            Toast.makeText(this, getString(R.string.on_stop_string), Toast.LENGTH_SHORT).show()
        }
        else{
            restartService()
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        restartService()
        super.onTaskRemoved(rootIntent);
    }

    private fun restartService() {
        val restartServiceIntent = Intent(getApplicationContext(), this::class.java)
        restartServiceIntent.setPackage(getPackageName())

        val restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        val alarmService = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun proceedSMS(smsMessageEvent: SMSMessageEvent) {
        currentSMSMessageEvent = smsMessageEvent
        try {
            when (smsMessageEvent.message.toLowerCase()) {
                sharedPref?.getString("call_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("call_enable", false) == true)
                        callPhone()
                }
                sharedPref?.getString("location_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("location_enable", false) == true)
                        sendLocation()
                }
                sharedPref?.getString("silent_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("silent_enable", false) == true)
                        setSoundToNoSound()
                }
                sharedPref?.getString("normal_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("normal_enable", false) == true)
                        setSoundToNormal()
                }
                sharedPref?.getString("wifi_networks_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("wifi_networks_enable", false) == true)
                        sendWifiNetworks()
                }
                sharedPref?.getString("report_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("report_enable", false) == true)
                        sendReport()
                }
            }
        } catch (e: Exception) {
            currentSMSMessageEvent = null
        }
    }


    fun getHumanRingerMode(ringerMode: Int): String {
        when (ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> {
                return getString(R.string.ringer_mode_vibrate)
            }
            AudioManager.RINGER_MODE_NORMAL -> {
                return getString(R.string.ringer_mode_normal)
            }
            AudioManager.RINGER_MODE_SILENT -> {
                return getString(R.string.ringer_mode_silent)
            }
        }
        return ""
    }

    fun getHumanWiFiStatus(): String {
        try {
            val wifiManager: WifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            when (wifiManager.wifiState) {
                WifiManager.WIFI_STATE_DISABLED -> {
                    return getString(R.string.wifi_state_disabled)
                }
                WifiManager.WIFI_STATE_DISABLING -> {
                    return getString(R.string.wifi_state_disabling)
                }
                WifiManager.WIFI_STATE_ENABLED -> {
                    return getString(R.string.wifi_state_enabled)
                }
                WifiManager.WIFI_STATE_ENABLING -> {
                    return getString(R.string.wifi_state_enabling)
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
        return ""
    }


    private fun sendReport() {
        var wifiNetworks = getWiFiNetworks()
        if (wifiNetworks?.size!! > 2)
            wifiNetworks = wifiNetworks?.subList(0, 2)
        val wifiMessage = prepareWiFiNetworksString(wifiNetworks)

        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = audioManager.ringerMode
        val ringerMode = getHumanRingerMode(mode)

        val batteryPct = getBatteryLevel()

        val sms = SmsManager.getDefault()
        val wifiStatus = getHumanWiFiStatus()
        var message: String = "${getString(R.string.ringer_mode)}:${ringerMode}\n " +
                "${getString(R.string.battery)}:${batteryPct}%\n " +
                "${wifiMessage} "
        message = Utils.transliterate(message)
        sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, message, null, null)
    }

    @SuppressLint("MissingPermission")
    private fun sendWifiNetworks() {
        val wifiNetworks = getWiFiNetworks()
        sendWifiNetworksSMS(wifiNetworks!!)
    }

    private fun getWiFiNetworks(): List<ScanResult>? {
        val wifiManager: WifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiNetworks = wifiManager.scanResults
        wifiNetworks.sortByDescending { it.level }
        return wifiNetworks
    }

    private fun sendWifiNetworksSMS(wifiNetworks: List<ScanResult>) {
        try {
            var message = prepareWiFiNetworksString(wifiNetworks)
            message = Utils.transliterate(message)
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, message, null, null)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    private fun prepareWiFiNetworksString(wifiNetworks: List<ScanResult>): String {
        val wifiStatus = getHumanWiFiStatus()
        var message = "${getString(R.string.wifi_label)}:${wifiStatus}\n\n"
        var i = 0
        for (network in wifiNetworks) {
            if (i < 5) {
                val level = WifiManager.calculateSignalLevel(network.level, 100)
                Log.d(LOG_TAG, "${network.SSID}: ${level}%")
                message += "${network.SSID}: ${level}%\n"
                i++
            }
        }
        return message
    }


    private fun setSoundToNormal() {
        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    private fun setSoundToNoSound() {
        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    @SuppressLint("MissingPermission")
    private fun sendLocation() {
        try {
            needLocationSMSToSend = true
            updateLocation()
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
            needLocationSMSToSend = false
        }
    }


    private fun sendLocationSMS() {
        try {
            if (currentSMSMessageEvent != null && location != null && needLocationSMSToSend) {
                val sms = SmsManager.getDefault()
                val batteryPct = getBatteryLevel()
                var locationString: String = "${R.string.accuracy}: ${location.accuracy} ${R.string.battery}:${batteryPct}%\n https://maps.google.com/maps?q=loc:${location.latitude},${location.longitude}"
                sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, locationString, null, null)
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
        needLocationSMSToSend = false
    }

    private fun getBatteryLevel(): Int {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = this.applicationContext.registerReceiver(null, iFilter)
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        return level
    }

    @SuppressLint("MissingPermission")
    private fun callPhone() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + currentSMSMessageEvent!!.phone)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onLocationChanged(p0: Location?) {
        location = p0!!
        sendLocationSMS()
        locationManager?.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        updateLocation()
    }

    @SuppressLint("MissingPermission")
    override fun onProviderEnabled(p0: String?) {
        updateLocation()
    }

    @SuppressLint("MissingPermission")
    override fun onProviderDisabled(p0: String?) {
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        try {
            locationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10F,
                    this
            );
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

}
