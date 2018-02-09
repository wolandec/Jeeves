package wolandec.jeeves

import android.os.Bundle
import android.preference.PreferenceFragment

class PrefFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
