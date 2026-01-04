package lat.pam.ydjob.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import lat.pam.ydjob.utils.UiState

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(email: String, pass: String, result: (UiState<FirebaseUser>) -> Unit) {
        result.invoke(UiState.Loading)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                result.invoke(UiState.Success(it.user!!))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }

    fun register(email: String, pass: String, name: String, role: String, result: (UiState<String>) -> Unit) {
        result.invoke(UiState.Loading)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "photoUrl" to ""
                    )

                    db.collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Registrasi Berhasil"))
                        }
                        .addOnFailureListener { e ->
                            result.invoke(UiState.Failure(e.message))
                        }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message))
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}