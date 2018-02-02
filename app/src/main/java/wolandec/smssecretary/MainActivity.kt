package wolandec.smssecretary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {
    val LOG_TAG = "myLogs"

    lateinit var myListView: ListView
    private lateinit var prefList: Array<out String>

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myListView = findViewById<ListView>(R.id.settingsList)
        prefList = getResources().getStringArray(R.array.list_preferences);

        fillSettigsList()
        setPrefListOnClickListener()
    }

    private fun setPrefListOnClickListener() {
        myListView.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                when (id) {
                    0L -> {
                        val intent = Intent(this@MainActivity, LocationActivity::class.java)
                        startActivity(intent)
                    }
                    1L -> {
                        val intent = Intent(this@MainActivity, CallActivity::class.java)
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
