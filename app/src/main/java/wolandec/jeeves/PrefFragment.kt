package wolandec.jeeves

import android.os.Bundle
import android.preference.PreferenceFragment
import android.widget.ListView


class PrefFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // remove dividers
        val list = view!!.findViewById<ListView>(android.R.id.list) as ListView
        list?.divider = null

    }

}
