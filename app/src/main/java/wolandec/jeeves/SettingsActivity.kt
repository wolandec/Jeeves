package wolandec.jeeves

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast




class SettingsActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    val LOG_TAG = this::class.java.simpleName
    var listView: ListView? = null

    public override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)
        registerBroadcastService()
        checkDoNotDisturb()
        fillList()
        listView?.setOnItemClickListener(this)
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position){
            0 ->{
                startActivity(Intent(this, LocationActivity::class.java))
            }
            1 ->{
                startActivity(Intent(this, CallActivity::class.java))
            }
        }
    }

    private fun fillList() {
        val adapter = ArrayAdapter<String>(this,
                R.layout.row_layout, R.id.label, resources.getStringArray(R.array.list_preferences))
        listView?.setAdapter(adapter)
    }

    @SuppressLint("NewApi")
    private fun checkDoNotDisturb() {
        if (Build.VERSION.SDK_INT < 23) {
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
