package wolandec.smssecretary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.telephony.SmsMessage
import android.util.Log


/**
 * Created by wolandec on 31.01.2018.
 */

class SMSReceiver() : BroadcastReceiver() {

    public var serviceActivity: ServiceActivity? = null

    override fun onReceive(context: Context?, intent: Intent?) {
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
//                        serviceActivity!!.proceedSMS(phone, message);
                    }
                } catch (e: Exception) {
                    Log.d("Exception caught", e.message);
                }
            }
        }
    }
}