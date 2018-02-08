package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class JeevesService() : Service(), LocationListener {

    lateinit var location: Location
    var brReceiver: SMSReceiver = SMSReceiver()

    constructor(parcel: Parcel) : this() {
        location = parcel.readParcelable(Location::class.java.classLoader)
    }

    private fun registerIntentReceiver() {
        registerReceiver(brReceiver,
                IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        Toast.makeText(applicationContext, getString(R.string.on_boot_string), Toast.LENGTH_LONG).show()
        registerIntentReceiver()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun proceedSMS(smsMessageEvent: SMSMessageEvent) {
        when (smsMessageEvent.message.decapitalize()) {
            "call" -> {
                callPhone(smsMessageEvent)
            }
            "location" -> {
                sendLocation(smsMessageEvent)
            }
            "no sound" -> {
                setSoundToNoSound(smsMessageEvent)
            }
            "sound" -> {
                setSoundToNormal(smsMessageEvent)
            }
        }
    }

    private fun setSoundToNormal(smsMessageEvent: SMSMessageEvent) {
        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    private fun setSoundToNoSound(smsMessageEvent: SMSMessageEvent) {
        val audioManager: AudioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    @SuppressLint("MissingPermission")
    private fun sendLocation(smsMessageEvent: SMSMessageEvent) {
        val locationManager = getLocationManager()
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        val sms = SmsManager.getDefault()
        val batteryPct = getBatteryLevel()
        var locationString: String = "Accuracy: ${location.accuracy} Power:${batteryPct}%\n https://maps.google.com/maps?q=loc:${location.latitude},${location.longitude}"
        sms.sendTextMessage(smsMessageEvent.phone, null, locationString, null, null)

        locationManager.removeUpdates(this)
    }

    private fun getBatteryLevel(): Int {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = this.applicationContext.registerReceiver(null, iFilter)
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        return level
    }

    @SuppressLint("MissingPermission")
    private fun callPhone(smsMessageEvent: SMSMessageEvent) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + smsMessageEvent.phone)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onLocationChanged(p0: Location?) {
        location = p0!!
    }

    @SuppressLint("MissingPermission")
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        val locationManager: LocationManager = getLocationManager()
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    override fun onProviderEnabled(p0: String?) {
        val locationManager: LocationManager = getLocationManager()
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    override fun onProviderDisabled(p0: String?) {
        val locationManager: LocationManager = getLocationManager()
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private fun getLocationManager(): LocationManager {
        val locationManager: LocationManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10F,
                this
        );
        return locationManager
    }


}
