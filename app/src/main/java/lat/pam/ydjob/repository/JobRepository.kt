package lat.pam.ydjob.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import lat.pam.ydjob.model.Application
import lat.pam.ydjob.model.Job
import lat.pam.ydjob.utils.UiState
import java.util.Date

class JobRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getJobsRealtime(callback: (UiState<List<Job>>) -> Unit): ListenerRegistration {
        val jobsRef = db.collection("jobs")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        return jobsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                callback(UiState.Failure(e.message ?: "Listen failed"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val jobList = snapshot.toObjects(Job::class.java)

                for (i in 0 until snapshot.size()) {
                    jobList[i].id = snapshot.documents[i].id
                }
                callback(UiState.Success(jobList))
            } else {
                callback(UiState.Failure("No data found"))
            }
        }
    }

    fun addJob(
        title: String,
        location: String,
        salaryMin: Long,
        salaryMax: Long,
        description: String,
        jobType: String,
        callback: (UiState<String>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(UiState.Failure("User not logged in"))
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val companyName = document.getString("companyName") ?: document.getString("name") ?: "Unknown Company"
                val companyLogo = document.getString("photoUrl") ?: ""

                val job = hashMapOf(
                    "title" to title,
                    "location" to location,
                    "salaryMin" to salaryMin,
                    "salaryMax" to salaryMax,
                    "description" to description,
                    "jobType" to jobType,
                    "companyId" to uid,
                    "companyName" to companyName,
                    "companyLogo" to companyLogo,
                    "createdAt" to com.google.firebase.Timestamp(Date())
                )

                db.collection("jobs").add(job)
                    .addOnSuccessListener {
                        callback(UiState.Success("Job posted successfully!"))
                    }
                    .addOnFailureListener { e ->
                        callback(UiState.Failure(e.message ?: "Failed to post job"))
                    }
            }
            .addOnFailureListener {
                callback(UiState.Failure("Failed to fetch company profile"))
            }
    }

    fun getApplicationsByCompany(callback: (UiState<List<Application>>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(UiState.Failure("User not logged in"))
            return
        }

        db.collection("applications")
            .whereEqualTo("companyId", uid)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Application::class.java)
                callback(UiState.Success(list))
            }
            .addOnFailureListener { e ->
                callback(UiState.Failure(e.message ?: "Error fetching applications"))
            }
    }

    fun getAllJobs(callback: (UiState<List<Job>>) -> Unit) {
        db.collection("jobs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val jobs = result.toObjects(Job::class.java)
                for (i in 0 until result.size()) {
                    jobs[i].id = result.documents[i].id
                }
                callback(UiState.Success(jobs))
            }
            .addOnFailureListener { e ->
                callback(UiState.Failure(e.message ?: "Error"))
            }
    }

    fun applyJob(job: Job, coverLetter: String, resumeUrl: String, result: (UiState<String>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            result.invoke(UiState.Failure("User not logged in"))
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val applicantName = document.getString("name") ?: "Unknown"
                val applicantPhoto = document.getString("photoUrl") ?: ""

                val applicationData = hashMapOf(
                    "jobId" to job.id,
                    "jobTitle" to job.title,
                    "companyId" to job.companyId,
                    "companyName" to job.companyName,
                    "userId" to uid,
                    "applicantName" to applicantName,
                    "applicantPhoto" to applicantPhoto,
                    "resumeUrl" to resumeUrl,
                    "coverLetter" to coverLetter,
                    "status" to "Applied",
                    "appliedAt" to com.google.firebase.Timestamp(Date())
                )

                db.collection("applications")
                    .add(applicationData)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success("Application Submitted Successfully!"))
                    }
                    .addOnFailureListener { e ->
                        result.invoke(UiState.Failure(e.message ?: "Submission failed"))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("Failed to fetch user data"))
            }
    }
}