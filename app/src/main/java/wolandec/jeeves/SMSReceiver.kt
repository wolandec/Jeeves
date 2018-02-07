package wolandec.jeeves

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import org.greenrobot.eventbus.EventBus


/**
 * Created by wolandec on 31.01.2018.
 */

class SMSReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("Receive", intent?.getAction())

        if (intent?.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent?.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {
            val i = Intent("wolandec.jeeves.BroadcastService")
            i.setClass(context, BroadcastService::class.java!!)
            try {
                context!!.startService(i)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
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
}