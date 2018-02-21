package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.Service
import android.content.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import android.os.BatteryManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.preference.PreferenceManager
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class JeevesService() : Service(), LocationListener {

    val LOG_TAG = this::class.java.simpleName
    val NOTIFY_ID = 1002


    private var needLocationSMSToSend: Boolean = false
    private var currentSMSMessageEvent: SMSMessageEvent? = null
    private var locationManager: LocationManager? = null
    var sharedPref: SharedPreferences? = null
    var sharedPrefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var intent: Intent? = null

    var location: Location? = null
    var brReceiver: JeevesReceiver = JeevesReceiver()

    constructor(parcel: Parcel) : this() {
        location = parcel.readParcelable(Location::class.java.classLoader)
    }

    private fun registerIntentReceiver() {
        registerReceiver(brReceiver,
                IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        val notification = Utils.getNotification(this)
        startForeground(NOTIFY_ID, notification)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        registerIntentReceiver()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        sharedPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sP, key ->
            if (key == "enable_jeeves") {
                proceedPrefChange()
            }
        }
        sharedPref?.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener)
    }

    fun proceedPrefChange() {
        if (sharedPref?.getBoolean("enable_jeeves", false) == false) {
            stopForeground(true);
            stopSelf()
        }
    }

    override fun onDestroy() {
        sharedPref?.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener)
        unregisterReceiver(brReceiver)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.intent = intent
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun proceedSMS(smsMessageEvent: SMSMessageEvent) {
        currentSMSMessageEvent = smsMessageEvent
        var pswd = ""
        if (!sharedPref?.getString("passwd", "").equals(""))
            pswd = sharedPref?.getString("passwd", "") + " "
        try {
            when (smsMessageEvent.message.toLowerCase()) {
                pswd + sharedPref?.getString("call_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("call_enable", false) == true)
                        callPhone()
                }
                pswd + sharedPref?.getString("location_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("location_enable", false) == true)
                        sendLocation()
                }
                pswd + sharedPref?.getString("silent_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("silent_enable", false) == true)
                        setSoundToNoSound()
                }
                pswd + sharedPref?.getString("normal_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("normal_enable", false) == true)
                        setSoundToNormal()
                }
                pswd + sharedPref?.getString("wifi_networks_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("wifi_networks_enable", false) == true)
                        sendWifiNetworks()
                }
                pswd + sharedPref?.getString("report_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("report_enable", false) == true)
                        sendReport()
                }
                pswd + sharedPref?.getString("find_sms", "")?.toLowerCase() -> {
                    if (sharedPref?.getBoolean("find_enable", false) == true)
                        startAlarm()
                }
            }
        } catch (e: Exception) {
            currentSMSMessageEvent = null
        }
    }

    private fun startAlarm() {
        val intent = Intent(this, AlarmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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
        val wifiCurState = startWiFiScan()
        if (!isGpsEnabled()) {
            proceedReportSend(currentSMSMessageEvent, wifiCurState)
            return
        }
        val scanResutsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                proceedReportSend(currentSMSMessageEvent, wifiCurState)
                unregisterReceiver(this)
            }
        }
        registerReceiver(scanResutsReceiver,
                IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION))
    }

    private fun proceedReportSend(currentSMSMessageEvent: SMSMessageEvent?, wifiCurState: Boolean) {
        var wifiNetworks = getWiFiScanResults(wifiCurState)
        val wifiMessage = prepareWiFiNetworksString(wifiNetworks, wifiCurState)

        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = audioManager.ringerMode
        val ringerMode = getHumanRingerMode(mode)

        val batteryPct = getBatteryLevel()

        val sms = SmsManager.getDefault()
        locationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var message = "${getString(R.string.gps_label)}:"
        if (!locationManager!!.isProviderEnabled("gps"))
            message += "${getString(R.string.wifi_state_disabled)}\n"
        else
            message += "${getString(R.string.wifi_state_enabled)}\n"

        message += "${getString(R.string.ringer_mode)}:${ringerMode}\n" +
                "${getString(R.string.battery)}:${batteryPct}%\n\n" +
                "${wifiMessage}"
        message = Utils.prepareMessageLength(message)
        sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, message, null, null)
    }


    @SuppressLint("MissingPermission")
    private fun sendWifiNetworks() {
        val wifiCurState = startWiFiScan()
        if (!isGpsEnabled()) {
            proceedWiFiNetworksSend(currentSMSMessageEvent, wifiCurState)
            return
        }
        if (!wifiCurState) {
            val scanResutsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    proceedWiFiNetworksSend(currentSMSMessageEvent, wifiCurState)
                    unregisterReceiver(this)
                }
            }
            registerReceiver(scanResutsReceiver,
                    IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION))
        } else {
            proceedWiFiNetworksSend(currentSMSMessageEvent, wifiCurState)
        }
    }

    private fun startWiFiScan(): Boolean {
        val wifiManager: WifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiCurState = wifiManager.isWifiEnabled
        if (!wifiCurState) {
            wifiManager.setWifiEnabled(true)
        }
        wifiManager.startScan()
        return wifiCurState
    }

    private fun proceedWiFiNetworksSend(currentSMSMessageEvent: SMSMessageEvent?, wifiCurState: Boolean) {
        val wifiNetworks = getWiFiScanResults(wifiCurState)
        sendWifiNetworksSMS(wifiNetworks, wifiCurState)
    }

    private fun getWiFiScanResults(wifiCurState: Boolean): List<ScanResult>? {
        val wifiManager: WifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiNetworks = wifiManager.scanResults
        wifiManager.setWifiEnabled(wifiCurState)
        val iterator = wifiNetworks.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.level == 0) {
                iterator.remove()
            }
        }
        wifiNetworks?.sortByDescending { it.level }
        return wifiNetworks
    }


    private fun sendWifiNetworksSMS(wifiNetworks: List<ScanResult>?, wifiCurState: Boolean) {
        try {
            var message = prepareWiFiNetworksString(wifiNetworks, wifiCurState)
            val sms = SmsManager.getDefault()
            message = Utils.prepareMessageLength(message)
            sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, message, null, null)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    private fun prepareWiFiNetworksString(wifiNetworks: List<ScanResult>?, wifiCurState: Boolean): String {
        var message = "${getString(R.string.wifi_label)}:"
        if (!wifiCurState) {
            message += "${getString(R.string.wifi_state_disabled)}\n"
        } else {
            message += "${getString(R.string.wifi_state_enabled)}\n"
        }
        message += "${getString(R.string.gps_label)}:${getGpsHumanStatus()}\n\n"

        var i = 0
        if (wifiNetworks != null && wifiNetworks.size > 0)
            for (network in wifiNetworks) {
                if (i < 5) {
                    val level = WifiManager.calculateSignalLevel(network.level, 100)
                    message += "${network.SSID}: ${level}%\n"
                    i++
                }
            }
        else {
            if (!isGpsEnabled()) {
                message += getString(R.string.GPS_needed_text)
            }
        }
        return message
    }

    private fun getGpsHumanStatus(): String {
        locationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager!!.isProviderEnabled("gps") == true)
            return getString(R.string.wifi_state_enabled)
        else
            return getString(R.string.wifi_state_disabled)
    }

    private fun isGpsEnabled(): Boolean {
        locationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled("gps")
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

            if (isGpsEnabled())
                updateLocation()
            else {
                sendLocationSMS(false)
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
            needLocationSMSToSend = false
        }
    }


    private fun sendLocationSMS(providerEnabled: Boolean) {
        try {
            if (currentSMSMessageEvent != null && needLocationSMSToSend) {
            val sms = SmsManager.getDefault()
            val batteryPct = getBatteryLevel()
            var message: String = ""
            if (!isGpsEnabled()){
                message = "${getString(R.string.gps_label)}:${getString(R.string.wifi_state_disabled)}\n"
            }
            message += "${getString(R.string.battery)}:$batteryPct%\n"
            if (location != null) {
                message += "${getString(R.string.accuracy)}:${location!!.accuracy}\n" +
                        "https://maps.google.com/maps?q=loc:${location!!.latitude},${location!!.longitude}"
            }
            message = Utils.prepareMessageLength(message)
            sms.sendTextMessage(currentSMSMessageEvent!!.phone, null, message, null, null)
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
        sendLocationSMS(false)
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
