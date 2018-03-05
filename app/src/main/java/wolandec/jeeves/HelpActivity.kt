package wolandec.jeeves

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide


class HelpActivity : IntroActivity() {

    val LOG_TAG = this::class.java.simpleName
    var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        buttonBackFunction = BUTTON_BACK_FUNCTION_BACK
        createPermissionsSlide()
        createOnBootStartupPermissionSlide()
        createDoNotDisturbSlide()
        onDisplayPopupMIUIPermissions()
        createHelloSlide()

//        Utils.changeNeedHelpDialogPrefTo(this, false)
    }


    @SuppressLint("ResourceType")
    private fun createHelloSlide() {

        addSlide(SimpleSlide.Builder()
                .title(R.string.hello_title)
                .description(R.string.hello_text)
//                .image(R.drawable.image_1)
                .background(R.color.colorPrimaryDark)
                .backgroundDark(R.color.colorAccent)
                .scrollable(true)
                .build())
    }

    private fun createPermissionsSlide() {
        if (!Utils.isPermissionGranted(this, Manifest.permission.SEND_SMS) ||
                !Utils.isPermissionGranted(this, Manifest.permission.RECEIVE_SMS) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CALL_PHONE) ||
                !Utils.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CAMERA) ||
                !Utils.isPermissionGranted(this, Manifest.permission.READ_PHONE_STATE) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CHANGE_WIFI_STATE)) {
            addSlide(SimpleSlide.Builder()
                    .title(R.string.perm_dialog_title)
                    .description(R.string.perm_dialog_text)
                    .background(R.color.colorPrimaryDark)
                    .backgroundDark(R.color.colorAccent)
                    .scrollable(true)
                    .buttonCtaLabel(R.string.perm_button_text)
                    .buttonCtaLabel(R.string.perm_button_text)
                    .buttonCtaClickListener(object : View.OnClickListener {
                        override fun onClick(p0: View?) {
                            ActivityCompat.requestPermissions(this@HelpActivity,
                                    arrayOf(Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.SEND_SMS,
                                            Manifest.permission.CALL_PHONE,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_SETTINGS,
                                            Manifest.permission.READ_PHONE_STATE,
                                            Manifest.permission.CHANGE_WIFI_STATE),
                                    1)

                            nextSlide()
                        }
                    })
                    .build())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(applicationContext)) {
                addSlide(SimpleSlide.Builder()
                        .background(R.color.colorPrimaryDark)
                        .backgroundDark(R.color.colorAccent)
                        .title(R.string.perm_write_settings_title)
                        .description(R.string.perm_write_settings_text)
                        .buttonCtaLabel(R.string.perm_button_text)
                        .buttonCtaClickListener(object : View.OnClickListener {
                            override fun onClick(p0: View?) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + packageName))
                                startActivityForResult(intent, 200)
                                nextSlide()
                            }
                        })
                        .build())
            }
        }
    }

    private fun createOnBootStartupPermissionSlide() {
        createXiaomiOnBootStartupPermission()
    }


    private fun createXiaomiOnBootStartupPermission() {
        val manufacturer = "xiaomi"
        if (manufacturer.equals(Build.MANUFACTURER, ignoreCase = true)) {
            addSlide(SimpleSlide.Builder()
                    .background(R.color.colorPrimaryDark)
                    .backgroundDark(R.color.colorAccent)
                    .title(resources.getString(R.string.startup_on_boot_dialog_title))
                    .description(resources.getString(R.string.startup_on_boot_dialog_text))
                    .buttonCtaLabel(R.string.perm_button_text)
                    .buttonCtaClickListener(object : View.OnClickListener {
                        override fun onClick(p0: View?) {
                            val intent = Intent()
                            intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                            startActivity(intent)
                            Utils.setFlagStartedAtBootToTrue(applicationContext)
                            nextSlide()
                        }
                    })
                    .build())
        }
    }

    fun onDisplayPopupMIUIPermissions() {
        if (Utils.isMIUI() && sharedPref?.getBoolean("miui_perms_are_checked", false) == false) {
            try {
                if (Utils.getMIUIVersion().equals("V5") ||
                        Utils.getMIUIVersion().equals("V6") ||
                        Utils.getMIUIVersion().equals("V7")) {
                    addSlide(SimpleSlide.Builder()
                            .background(R.color.colorPrimaryDark)
                            .backgroundDark(R.color.colorAccent)
                            .title(resources.getString(R.string.xiaomi_perm_dialog_title))
                            .description(resources.getString(R.string.xiaomi_perm_dialog_text))
                            .buttonCtaLabel(R.string.perm_button_text)
                            .buttonCtaClickListener(object : View.OnClickListener {
                                override fun onClick(p0: View?) {
                                    val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getPackageName());
                                    startActivity(localIntent);
                                    Utils.setMIUIPermsAreCheckedToTrue(applicationContext)
                                    nextSlide()
                                }
                            })
                            .build())
                }
                if (Utils.getMIUIVersion().equals("V8")) {
                    // MIUI 8
                    addSlide(SimpleSlide.Builder()
                            .background(R.color.colorPrimaryDark)
                            .backgroundDark(R.color.colorAccent)
                            .title(resources.getString(R.string.xiaomi_perm_dialog_title))
                            .description(resources.getString(R.string.xiaomi_perm_dialog_text))
                            .buttonCtaLabel(R.string.perm_button_text)
                            .buttonCtaClickListener(object : View.OnClickListener {
                                override fun onClick(p0: View?) {
                                    val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getPackageName());
                                    startActivity(localIntent);
                                    Utils.setMIUIPermsAreCheckedToTrue(applicationContext)
                                    nextSlide()
                                }
                            })
                            .build())
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
            addSlide(SimpleSlide.Builder()
                    .background(R.color.colorPrimaryDark)
                    .backgroundDark(R.color.colorAccent)
                    .title(resources.getString(R.string.dont_disturb_perm_dialog_title))
                    .description(resources.getString(R.string.dont_disturb_perm_dialog_text))
                    .buttonCtaLabel(R.string.perm_button_text)
                    .buttonCtaClickListener(object : View.OnClickListener {
                        override fun onClick(p0: View?) {
                            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            startActivity(intent)
                            nextSlide()
                        }
                    })
                    .build())
        }
    }


}
