package lat.pam.ydjob.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize // <--- 1. Import ini
import kotlinx.parcelize.RawValue
import java.util.Date

@Parcelize
data class Job(
    @DocumentId
    var id: String = "",
    val title: String = "",
    val companyName: String = "",
    val location: String = "",
    val description: String = "",
    val salaryMin: Long = 0,
    val salaryMax: Long = 0,
    val jobType: String = "",
    val companyId: String = "",
    val companyLogo: String = "",
    val workplace: String = "",

    @ServerTimestamp
    val createdAt: @RawValue Date? = null
) : Parcelable