package lat.pam.ydjob

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = HashMap<String, String>()
        config["cloud_name"] = "ds3pu1pzg"

        MediaManager.init(this, config)
    }
}