package wolandec.jeeves

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_alarm.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class AlarmActivity : AppCompatActivity() {

    private val SOUND_STREAM = AudioManager.STREAM_MUSIC
    private val BLINK_DELAY: Long = 500

    private var mediaPlayer: MediaPlayer? = null
    private var cameraManager: CameraManager? = null
    private var camera: Camera? = null
    private var isFlashOn = false
    private val LOG_TAG = this::class.java.simpleName
    private var params: android.hardware.Camera.Parameters? = null
    private var originalVolume: Int? = null
    private var mAudioManager: AudioManager? = null
    private var startBlinkFlash = false
    private var timer: Timer? = null
    private var flashTimer: Timer? = null
    private var curBrightness: Int? = null


    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        stopAlarm()
        this.finish()
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_alarm)
        val text = getIntent().getStringExtra("text")
        if (text != null)
            findViewById<TextView>(R.id.fullscreen_content).text = text

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreen_content.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)
        EventBus.getDefault().register(this)
        startAlarm()
        super.onCreate(savedInstanceState)
    }

    fun setMaxBrightness() {
        setCurrentBrightness()
        setBrightness(255)
    }

    fun setBrightness(brightness: Int) {
        try {
            var brightness = brightness

            if (brightness < 0)
                brightness = 0
            else if (brightness > 255)
                brightness = 255

            val cResolver = this.contentResolver
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    fun setCurrentBrightness() {
        try {
            val cResolver = this.contentResolver
            curBrightness = Settings.System.getInt(
                    cResolver,
                    Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun startAlarm() {
        playAlarm()
        starBlinkWithFlash()
        startScreenBlink()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        stopAlarm()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun stopAlarm() {
        stopBlinkWithFlash()
        stopScreenBlink()
        stopPlay()
    }

    fun setMaxVolume() {
        try {
            mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            originalVolume = mAudioManager?.getStreamVolume(SOUND_STREAM)
            mAudioManager?.setStreamVolume(SOUND_STREAM, mAudioManager?.getStreamMaxVolume(SOUND_STREAM)!!, 0)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    fun playAlarm() {
        val resID = this.getResources().getIdentifier("alarm", "raw", this.packageName)
        mediaPlayer = MediaPlayer.create(this, resID)
        mediaPlayer?.isLooping = true
        setMaxVolume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer?.setAudioAttributes(AudioAttributes.Builder()
                    .setLegacyStreamType(SOUND_STREAM).build())
        } else {
            mediaPlayer?.setAudioStreamType(SOUND_STREAM)
        }
        mediaPlayer?.start()
    }

    fun stopPlay() {
        setOriginalVolume()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setOriginalVolume() {
        try {
            mAudioManager?.setStreamVolume(SOUND_STREAM, originalVolume!!, 0)
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }
    }

    private fun starBlinkWithFlash() {
        try {
            if (!this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
                return
            if (Utils.isPermissionGranted(this, android.Manifest.permission.CAMERA))
                return
            getCamera(this)
            startBlinkFlash()
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }

    }

    private fun startBlinkFlash() {
        flashTimer = Timer()
        flashTimer?.schedule(object : TimerTask() {
            override fun run() {
                EventBus.getDefault().post(FlashChangeEvent(true))
            }
        }, 0, BLINK_DELAY)
    }

    private fun stopScreenBlink() {
        setBrightness(curBrightness!!)
        startBlinkFlash = false
        timer?.cancel()
    }

    private fun startScreenBlink() {
        setMaxBrightness()
        var toggle = false
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (toggle)
                    EventBus.getDefault().post(ScreenChangeEvent(R.color.colorPrimary, R.color.colorAccent))
                else
                    EventBus.getDefault().post(ScreenChangeEvent(R.color.colorAccent, R.color.colorPrimary))
                toggle = !toggle
            }
        }, 0, BLINK_DELAY)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setScreenColor(screenChangeEvent: ScreenChangeEvent) {
        findViewById<FrameLayout>(R.id.frame).setBackgroundColor(ContextCompat.getColor(this, screenChangeEvent.color))
        findViewById<TextView>(R.id.fullscreen_content).setTextColor(ContextCompat.getColor(this, screenChangeEvent.textColor))
    }

    fun stopBlinkWithFlash() {
        flashTimer?.cancel()
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            return
        turnOffFlash()
        camera?.release()
    }

    private fun getCamera(context: Context) {
        if (cameraManager == null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager = context.getSystemService(CameraManager::class.java)
                } else {
                    camera = Camera.open()
                    params = camera?.getParameters()
                }
            } catch (e: RuntimeException) {
                Log.d(LOG_TAG, e.toString())
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun toggleFlash(flashChangeEvent: FlashChangeEvent) {
        if (isFlashOn) {
            turnOffFlash()
        } else {
            turnOnFlash()
        }
    }

    private fun turnOnFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager?.setTorchMode(cameraManager?.getCameraIdList()!![0], true)
        } else {
            if (camera == null || params == null) {
                return
            }
            params = camera?.getParameters()

            params?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
            camera?.setParameters(params)
            camera?.startPreview()
        }
        isFlashOn = true
    }

    private fun turnOffFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager?.setTorchMode(cameraManager?.getCameraIdList()!![0], false)
        } else {
            if (camera == null || params == null) {
                return
            }
            params = camera?.getParameters()
            params?.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF)
            camera?.setParameters(params)
            camera?.stopPreview()
        }
        isFlashOn = false
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
