package wolandec.jeeves

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import android.widget.Toast.makeText as makeText1


/**
 * Created by wolandec on 31.01.2018.
 */

class JeevesReceiver() : BroadcastReceiver() {

    val LOG_TAG = this::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.getAction().equals("com.wolandec.jeeves.startService")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(Intent(context, JeevesService::class.java))
            } else {
                context?.startService(Intent(context, JeevesService::class.java))
            }
            Toast.makeText(context, "Start from receiver", Toast.LENGTH_SHORT).show()
        }

        if (intent?.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent?.getAction().equals("android.intent.action.QUICKBOOT_POWERON") ||
                intent?.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED) ||
                intent?.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            startJeevesService(context)
        }

        if (intent?.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            EventBus.getDefault().post(BatteryLowEvent())
        }

        if (intent?.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            val bundle = intent?.getExtras()
            var messages: Array<SmsMessage?>
            var phone: String
            if (bundle != null) {
                try {
                    val pdus = bundle!!.get("pdus") as Array<Any>
                    messages = arrayOfNulls<SmsMessage>(pdus.size)
                    val sms = HashMap<String, String>();
                    for (i in messages.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        phone = messages[i]!!.getOriginatingAddress()
                        val message = messages[i]!!.getMessageBody()
                        EventBus.getDefault().post(SMSMessageEvent(phone, message))
                    }
                } catch (e: Exception) {
                    Log.d("Exception caught", e.message);
                }
            }
        }
    }

    fun startJeevesService(context: Context?) {
        val i = Intent("wolandec.jeeves.JeevesService")
        i.setClass(context, JeevesService::class.java!!)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context!!.startForegroundService(i)
            } else {
                context!!.startService(i)
            }
            Utils.setFlagStartedAtBootToTrue(context)
        } catch (e: Exception) {
            makeText1(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

}