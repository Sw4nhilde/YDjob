package lat.pam.ydjob.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Application(
    var id: String = "",
    val jobId: String = "",
    val userId: String = "",
    val companyId: String = "",
    val applicantName: String = "",
    val applicantEmail: String = "",
    val applicantPhoto: String = "",
    val applicantPhone: String = "",
    val resumeUrl: String = "",
    val status: String = "Pending",
    @ServerTimestamp
    val appliedAt: Date? = null
)