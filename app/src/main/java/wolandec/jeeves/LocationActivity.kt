package wolandec.jeeves

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import wolandec.jeeves.R

class LocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        createLocationLitener()

    }

fun createLocationLitener(){
    val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;

    findViewById<TextView>(R.id.textView3).setOnClickListener { view ->
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this.locationListener);
        } catch(ex: SecurityException) {
            Log.d("DEBUG", "Security Exception, no location available");
        }
        catch(ex: Exception) {
            Log.d("DEBUG", ex.message);
        }
    }
}

    //define the listener
private val locationListener: LocationListener = object : LocationListener {
    override fun onLocationChanged(location: Location) {
        Log.d("DEBUG","" + location.longitude + ":" + location.latitude);
    }
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
}
