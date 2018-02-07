package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode




class BroadcastService : Service() {

    var brReceiver: SMSReceiver = SMSReceiver()

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
            "сall" -> {
                callPhone(smsMessageEvent)
            }
            "location" ->{
                sendLocation(smsMessageEvent)
            }
        }
    }

    private fun sendLocation(smsMessageEvent: SMSMessageEvent) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsMessageEvent.phone, null, "Hello", null, null)
    }

    @SuppressLint("MissingPermission")
    private fun callPhone(smsMessageEvent: SMSMessageEvent) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + smsMessageEvent.phone)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
