package wolandec.jeeves

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Telephony
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
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

        registerIntentReceiver()

        startServiceActivity()
        setContentView(R.layout.activity_main)

        myListView = findViewById<ListView>(R.id.settingsList)
        prefList = getResources().getStringArray(R.array.list_preferences);

        fillSettigsList()
        setPrefListOnClickListener()
    }

    private fun registerIntentReceiver() {
        var brReceiver: SMSReceiver = SMSReceiver()
        registerReceiver(brReceiver,
                IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        registerReceiver(brReceiver,
                IntentFilter(Intent.ACTION_BOOT_COMPLETED))
    }

    private fun startServiceActivity() {
        val i = Intent(this@SettingsActivity, ServiceActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setPrefListOnClickListener() {
        myListView.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                when (id) {
                    0L -> {
                        val intent = Intent(this@SettingsActivity, LocationActivity::class.java)
                        startActivity(intent)
                    }
                    1L -> {
                        val intent = Intent(this@SettingsActivity, CallActivity::class.java)
                        startActivity(intent)
                    }
                    else -> {
                    }
                }
            }
        })
    }

    fun fillSettigsList() {
        val adapter: ArrayAdapter<String>
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prefList)
        myListView.setAdapter(adapter)
    }

}
