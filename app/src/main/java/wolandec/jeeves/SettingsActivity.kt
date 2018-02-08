package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


class SettingsActivity : AppCompatActivity() {
    val LOG_TAG = this::class.java.simpleName

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerBroadcastService()
        checkDoNotDisturb()
    }

    @SuppressLint("NewApi")
    fun checkDoNotDisturb(){
        if( Build.VERSION.SDK_INT < 23 ) {
            return;
        }
        val n = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (n.isNotificationPolicyAccessGranted) {
        } else {
            // Ask the user to grant access
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun registerBroadcastService() {
        val i = Intent("wolandec.jeeves.JeevesService")
        i.setClass(this, JeevesService::class.java!!)
        try {
            this!!.startService(i)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

}
