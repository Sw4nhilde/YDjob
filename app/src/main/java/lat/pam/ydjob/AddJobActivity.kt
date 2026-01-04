package lat.pam.ydjob

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityAddJobBinding
import lat.pam.ydjob.model.Job

class AddJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddJobBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { finish() }

        binding.btnPostJob.setOnClickListener {
            postJob()
        }
    }

    private fun postJob() {
        val uid = auth.currentUser?.uid ?: return

        val title = binding.etJobTitle.text.toString().trim()
        val type = binding.etJobType.text.toString().trim()
        val loc = binding.etJobLocation.text.toString().trim()
        val desc = binding.etJobDesc.text.toString().trim()
        val min = binding.etSalaryMin.text.toString().toLongOrNull() ?: 0
        val max = binding.etSalaryMax.text.toString().toLongOrNull() ?: 0

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPostJob.isEnabled = false
        binding.btnPostJob.text = "Posting..."

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val companyName = document.getString("companyName") ?: "Unknown Company"
                val companyLogo = document.getString("photoUrl") ?: ""

                val newJob = hashMapOf(
                    "title" to title,
                    "jobType" to type,
                    "location" to loc,
                    "description" to desc,
                    "salaryMin" to min,
                    "salaryMax" to max,
                    "companyId" to uid,
                    "companyName" to companyName,
                    "companyLogo" to companyLogo,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("jobs").add(newJob)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to post job", Toast.LENGTH_SHORT).show()
                        binding.btnPostJob.isEnabled = true
                        binding.btnPostJob.text = "Post Job Now"
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch company info", Toast.LENGTH_SHORT).show()
            }
    }
}