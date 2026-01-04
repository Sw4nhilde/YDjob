package lat.pam.ydjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityCompanyDetailBinding

class CompanyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var companyId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackCompany.setOnClickListener { finish() }

        companyId = intent.getStringExtra("EXTRA_COMPANY_ID") ?: ""

        if (companyId.isNotEmpty()) {
            fetchCompanyProfile(companyId)
            checkAvailableJobs(companyId)
        } else {
            Toast.makeText(this, "Company ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchCompanyProfile(id: String) {
        db.collection("users").document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("companyName") ?: "No Name"
                    val location = document.getString("location") ?: "Unknown Location"
                    val description = document.getString("description") ?: "No description available."
                    val phone = document.getString("phone") ?: "-"
                    val email = document.getString("email") ?: "-"
                    val website = document.getString("website") ?: "-"
                    val photoUrl = document.getString("photoUrl")

                    binding.tvCompanyName.text = name
                    binding.tvCompanyLoc.text = location
                    binding.tvAbout.text = description
                    binding.tvPhone.text = phone
                    binding.tvEmail.text = email
                    binding.tvWebsite.text = website

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCompanyLogo)
                    }

                    setupActionClicks(website, email, phone)
                } else {
                    Toast.makeText(this, "Company profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAvailableJobs(id: String) {
        db.collection("jobs")
            .whereEqualTo("companyId", id)
            .get()
            .addOnSuccessListener { documents ->
                val jobCount = documents.size()
                if (jobCount > 0) {
                    binding.btnAvailableJobs.text = "See $jobCount Available Jobs"
                    binding.btnAvailableJobs.isEnabled = true

                    binding.btnAvailableJobs.setOnClickListener {
                        Toast.makeText(this, "Showing $jobCount jobs...", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    binding.btnAvailableJobs.text = "No Available Jobs"
                    binding.btnAvailableJobs.isEnabled = false
                }
            }
            .addOnFailureListener {
                binding.btnAvailableJobs.text = "Check Available Jobs"
            }
    }

    private fun setupActionClicks(web: String, email: String, phone: String) {
        binding.tvWebsite.setOnClickListener {
            if (web != "-" && web.isNotEmpty()) {
                val url = if (!web.startsWith("http")) "http://$web" else web
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) { }
            }
        }
    }
}

