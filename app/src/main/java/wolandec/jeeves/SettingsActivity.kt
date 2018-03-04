package wolandec.jeeves

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast


class SettingsActivity : AppCompatActivity(), LoginDialogFragment.LoginDialogListener {


    val LOG_TAG = this::class.java.simpleName
    var sharedPref: SharedPreferences? = null
    var sharedPrefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    var appPasswd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        checkPermissions()
        if (sharedPref?.getBoolean("enable_jeeves", false) == true) {
            registerBroadcastService()
        }
        sharedPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sP, key ->
            if (key == "enable_jeeves") {
                if (sharedPref?.getBoolean("enable_jeeves", false) == true)
                    registerBroadcastService()
            }
        }
        sharedPref?.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);

        if (sharedPref?.getString("app_passwd", "") != appPasswd)
            showLoginDialog(false)

        startActivity(Intent(this, HelpActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName
        menu?.getItem(2)?.title = menu?.getItem(2)?.title.toString() + ": " + versionName
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.google_play -> {
                val appPackageName = packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
                }
            }
            R.id.help -> {
                try {
                    startActivity(Intent(this,HelpActivity::class.java))
                } catch (e: Exception) {
                    Log.d(LOG_TAG, e.toString())
                }
            }
        }
        return true
    }

    private fun showLoginDialog(invalidPassword: Boolean) {
        val loginDialog = LoginDialogFragment()
        loginDialog.isCancelable = false
        loginDialog.invalidPassword = invalidPassword
        loginDialog.show(getSupportFragmentManager(), "LoginDialogFragment");
    }

    fun checkPermissions() {
        checkRegularPermissions()
        checkOnBootStartupPermission()
        checkDoNotDisturb()
    }

    private fun checkOnBootStartupPermission() {
        checkXiaomiOnBootStartupPermission()
    }


    private fun checkXiaomiOnBootStartupPermission() {
        if (sharedPref?.getBoolean("started_at_boot", false) == true)
            return

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
                        Utils.setFlagStartedAtBootToTrue(this@SettingsActivity)
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
                                Utils.setMIUIPermsAreCheckedToTrue(this@SettingsActivity)
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
                                Utils.setMIUIPermsAreCheckedToTrue(this@SettingsActivity)
                            })
                            .setNegativeButton(android.R.string.no, null).show()
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
            }
        }
    }

    private fun checkRegularPermissions() {
        onDisplayPopupMIUIPermissions()

        if (!Utils.isPermissionGranted(this, Manifest.permission.SEND_SMS) ||
                !Utils.isPermissionGranted(this, Manifest.permission.RECEIVE_SMS) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CALL_PHONE) ||
                !Utils.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CAMERA) ||
                !Utils.isPermissionGranted(this, Manifest.permission.WRITE_SETTINGS) ||
                !Utils.isPermissionGranted(this, Manifest.permission.READ_PHONE_STATE) ||
                !Utils.isPermissionGranted(this, Manifest.permission.CHANGE_WIFI_STATE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_SETTINGS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE),
                    1)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(applicationContext)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + packageName))
                    startActivityForResult(intent, 200)

                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun checkDoNotDisturb() {
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

    private fun registerBroadcastService() {
        val i = Intent("wolandec.jeeves.JeevesService")
        i.setClass(this, JeevesService::class.java!!)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this!!.startForegroundService(i)
            } else {
                this!!.startService(i)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDialogNegativeClick(dialog: LoginDialogFragment) {
        this.finish()
    }

    override fun onDialogPositiveClick(dialog: LoginDialogFragment) {
        if (dialog?.passView?.text!!.toString().equals(sharedPref?.getString("app_passwd", ""))) {

        } else {
            showLoginDialog(true)
        }
    }


}
