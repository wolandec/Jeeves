package wolandec.jeeves

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.MessageButtonBehaviour
import agency.tango.materialintroscreen.SlideFragmentBuilder
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View

class HelpActivity : MaterialIntroActivity() {

    val LOG_TAG = this::class.java.simpleName
    var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        createPermissionsSlide()
        createDoNotDisturbSlide()
        createOnBootStartupPermissionSlide()
    }

    private fun createPermissionsSlide() {

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.colorAccent)
                .buttonsColor(R.color.colorAccent)
                .possiblePermissions(arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_SETTINGS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE))
                .neededPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS))
//                .image(agency.tango.materialintroscreen.R.drawable.ic_next)
                .title("Разрешения")
                .description("Разрешения надо ")
                .build(),
                MessageButtonBehaviour(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        showMessage("We provide solutions to make you love your work");
                    }
                }, "Work with love"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(applicationContext)) {
                addSlide(SlideFragmentBuilder()
                        .backgroundColor(R.color.colorAccent)
                        .buttonsColor(R.color.colorAccent)
//                .image(agency.tango.materialintroscreen.R.drawable.ic_next)
                        .title("Разрешения")
                        .description("Разрешения надо ")
                        .build(),
                        MessageButtonBehaviour(object : View.OnClickListener {
                            override fun onClick(v: View) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + packageName))
                                startActivityForResult(intent, 200)
                            }
                        }, "Work with love"))
            }
        }
    }

    private fun createOnBootStartupPermissionSlide() {
        createXiaomiOnBootStartupPermission()
    }


    private fun createXiaomiOnBootStartupPermission() {
        val manufacturer = "xiaomi"
        if (manufacturer.equals(Build.MANUFACTURER, ignoreCase = true)) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.startup_on_boot_dialog_title)
                    .setMessage(R.string.startup_on_boot_dialog_text)
                    .setIcon(R.drawable.ic_icon)
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                        val intent = Intent()
                        intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                        startActivity(intent)
                        Utils.setFlagStartedAtBootToTrue(this)
                    })
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }

    fun onDisplayPopupMIUIPermissions() {
        if (Utils.isMIUI() && sharedPref?.getBoolean("miui_perms_are_checked", false) == false) {
            try {
                if (Utils.getMIUIVersion().equals("V5") ||
                        Utils.getMIUIVersion().equals("V6") ||
                        Utils.getMIUIVersion().equals("V7")) {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.perm_dialog_title)
                            .setMessage(R.string.perm_dialog_text)
                            .setIcon(R.drawable.ic_icon)
                            .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                                val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                startActivity(localIntent);
                                Utils.setMIUIPermsAreCheckedToTrue(this)
                            })
                            .setNegativeButton(android.R.string.no, null).show()

                }
                if (Utils.getMIUIVersion().equals("V8")) {
                    // MIUI 8
                    AlertDialog.Builder(this)
                            .setTitle(R.string.perm_dialog_title)
                            .setMessage(R.string.perm_dialog_text)
                            .setIcon(R.drawable.ic_icon)
                            .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                                val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                startActivity(localIntent);
                                Utils.setMIUIPermsAreCheckedToTrue(this)
                            })
                            .setNegativeButton(android.R.string.no, null).show()
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createDoNotDisturbSlide() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        val n = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (n.isNotificationPolicyAccessGranted) {
        } else {
            // Ask the user to grant accessAlertDialog.Builder(this)
            AlertDialog.Builder(this)
                    .setTitle(R.string.dont_disturb_perm_dialog_title)
                    .setMessage(R.string.dont_disturb_perm_dialog_text)
                    .setIcon(R.drawable.ic_icon)
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        startActivity(intent)
                    })
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }


}
