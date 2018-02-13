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

        fun transliterate(message: String): String {
            val abcCyr = charArrayOf(' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
            val abcLat = arrayOf(" ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
            val builder = StringBuilder()
            for (i in 0 until message.length) {
                var specialSymbol = true;
                for (x in abcCyr.indices) {
                    if (message[i] == abcCyr[x]) {
                        builder.append(abcLat[x])
                        specialSymbol = false
                    }
                }
                if (specialSymbol)
                    builder.append(message[i])
            }
            return builder.toString()
        }

    }
}