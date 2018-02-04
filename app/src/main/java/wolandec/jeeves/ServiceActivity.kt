package wolandec.jeeves

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ServiceActivity : Activity() {

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        Toast.makeText(applicationContext, getString(R.string.on_boot_string), Toast.LENGTH_LONG).show()
        super.finish()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun proceedSMS(smsMessageEvent: SMSMessageEvent) {
        when (smsMessageEvent.message) {
            "Call" -> {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + smsMessageEvent.phone)
                startActivity(intent)
            }
        }
    }
}