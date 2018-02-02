package wolandec.smssecretary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import org.greenrobot.eventbus.EventBus


/**
 * Created by wolandec on 31.01.2018.
 */

class SMSReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

//        /****** For Start Activity *****/
//        val i = Intent(context, MainActivity::class.java)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context!!.startActivity(i)
//        if (intent?.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            MainActivity.getInstance()
//            EventBus.getDefault().post(SMSMessageEvent("312324", "Call"))
////            EventBus.getDefault().post(SMSMessageEvent("3213123123", "Call"))
////            Toast.makeText(context, "hello", Toast.LENGTH_LONG).show()
//        }

//        /***** For start Service  ****/
//        val myIntent = Intent(context, ServiceClassName::class.java)
//        context.startService(myIntent)


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