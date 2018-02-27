
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPairGenerator


/**
 * Created by wolandec on 19.02.18.
 */

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun getKeyStore() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.genKeyPair()
    }

}
