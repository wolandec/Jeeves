package wolandec.jeeves

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class CallActivity : AppCompatActivity() {

    val LOG_TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
    }
}
