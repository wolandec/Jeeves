package wolandec.smssecretary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class ServiceActivity : AppCompatActivity() {

    val broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

        fun proceedSMS(phone: String?, message: String?) {
            when (message) {
                "Call" -> {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:" + phone)
                    startActivity(intent)
                }
            }
        }
}
