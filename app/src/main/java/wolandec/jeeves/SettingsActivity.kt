package wolandec.jeeves

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import android.widget.Toast


class SettingsActivity : AppCompatActivity() {
    val LOG_TAG = "myLogs"

    lateinit var myListView: ListView
    private lateinit var prefList: Array<out String>

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerBroadcastService()
    }

    private fun registerBroadcastService() {
        val i = Intent("wolandec.jeeves.BroadcastService")
        i.setClass(this, BroadcastService::class.java!!)
        try {
            this!!.startService(i)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

}
