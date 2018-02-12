package wolandec.jeeves

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

/**
 * Created by wolandec on 12.02.18.
 */

class Utils {

    companion object {
        val LOG_TAG = this::class.java.simpleName

        fun setFlagStartedAtBootToTrue(context: Context?) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()
            sharedPrefEditor.putBoolean("started_at_boot", true)
            sharedPrefEditor.commit()
        }

        fun setMIUIPermsAreCheckedToTrue(context: Context?) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()
            sharedPrefEditor.putBoolean("miui_perms_are_checked", true)
            sharedPrefEditor.commit()
        }

        fun isMIUI(): Boolean {
            val device = Build.MANUFACTURER;
            if (device.equals("Xiaomi")) {
                try {
                    val prop = Properties();
                    prop.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")));
                    return prop.getProperty("ro.miui.ui.version.code", null) != null
                            || prop.getProperty("ro.miui.ui.version.name", null) != null
                            || prop.getProperty("ro.miui.internal.storage", null) != null;
                } catch (e: IOException) {
                    e.printStackTrace();
                }

            }
            return false;
        }

        fun getMIUIVersion(): String {
            val device = Build.MANUFACTURER;
            if (device.equals("Xiaomi")) {
                try {
                    val prop = Properties();
                    prop.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")));
                    return prop.getProperty("ro.miui.ui.version.name", null)
                } catch (e: IOException) {
                    Log.d(LOG_TAG, e.toString())
                    return ""
                }
            }
            return ""
        }

    }
}