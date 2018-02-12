package wolandec.jeeves

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by wolandec on 12.02.18.
 */

class Utils {
    companion object {
        fun setFlagStartedAtBootToTrue(context: Context?) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()
            sharedPrefEditor.putBoolean("started_at_boot", true)
            sharedPrefEditor.commit()
        }

    }
}