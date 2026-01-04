package lat.pam.ydjob.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import lat.pam.ydjob.utils.UiState


class ProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun getUserRole(result: (UiState<String>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            result.invoke(UiState.Failure("User belum login"))
            return
        }

        result.invoke(UiState.Loading)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "seeker"
                    result.invoke(UiState.Success(role))
                } else {
                    result.invoke(UiState.Failure("Data user tidak ditemukan"))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }

    fun updateCompanyProfile(companyName: String, desc: String, location: String, result: (UiState<String>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        result.invoke(UiState.Loading)

        val companyData = hashMapOf(
            "companyName" to companyName,
            "description" to desc,
            "location" to location,
            "userId" to uid
        )

        db.collection("companies").document(uid)
            .set(companyData, SetOptions.merge())
            .addOnSuccessListener {
                result.invoke(UiState.Success("Profil Perusahaan Update!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }

    fun updateSeekerProfile(skills: String, experience: String, result: (UiState<String>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        result.invoke(UiState.Loading)

        val skillsArray = skills.split(",").map { it.trim() }

        val seekerData = hashMapOf(
            "skills" to skillsArray,
            "experience" to experience,
            "userId" to uid
        )

        db.collection("job_seekers").document(uid)
            .set(seekerData, SetOptions.merge())
            .addOnSuccessListener {
                result.invoke(UiState.Success("Profil Update!"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }
}