package lat.pam.ydjob

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityApplicantProfileBinding

class ApplicantProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicantProfileBinding
    private val db = FirebaseFirestore.getInstance()

    private var applicantId: String = ""
    private var resumeUrl: String = ""
    private var phoneNumber: String = ""
    private var emailAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicantProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applicantId = intent.getStringExtra("APPLICANT_ID") ?: ""

        if (applicantId.isEmpty()) {
            Toast.makeText(this, "Data Pelamar Tidak Ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        fetchApplicantData(applicantId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.cvResume.setOnClickListener {
            if (resumeUrl.isNotEmpty()) {
                downloadResume(resumeUrl)
            } else {
                Toast.makeText(this, "Resume tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCall.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEmail.setOnClickListener {
            if (emailAddress.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$emailAddress")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Aplikasi Email tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchApplicantData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "No Name"
                    val role = doc.getString("jobTitle") ?: "Job Seeker"
                    val bio = doc.getString("about") ?: "No bio available."
                    val photoUrl = doc.getString("photoUrl") ?: ""

                    resumeUrl = doc.getString("resumeUrl") ?: ""
                    phoneNumber = doc.getString("phone") ?: ""
                    emailAddress = doc.getString("email") ?: ""

                    binding.tvName.text = name
                    binding.tvRole.text = role
                    binding.tvBio.text = bio

                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivProfile)
                    }

                    if (resumeUrl.isEmpty()) {
                        binding.tvResumeLabel.text = "No Resume Uploaded"
                        binding.cvResume.setCardBackgroundColor(resources.getColor(android.R.color.darker_gray))
                        binding.cvResume.isEnabled = false
                    }

                    val skills = doc.get("skills") as? List<String>
                    if (skills != null) populateSkills(skills)

                    val experiences = doc.get("experiences") as? List<Map<String, String>>
                    if (experiences != null) populateExperiences(experiences)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateSkills(skills: List<String>) {
        binding.chipGroupSkills.removeAllViews()
        skills.forEach { skill ->
            val chip = Chip(this)
            chip.text = skill
            binding.chipGroupSkills.addView(chip)
        }
    }

    private fun populateExperiences(expList: List<Map<String, String>>) {
        binding.llExperienceContainer.removeAllViews()

        for (exp in expList) {
            val title = exp["title"] ?: ""
            val company = exp["company"] ?: ""
            val date = exp["date"] ?: ""
            val desc = exp["description"] ?: ""

            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 24) }
                radius = 12f
                cardElevation = 2f
                setContentPadding(24, 24, 24, 24)
            }

            val innerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            val tvTitle = TextView(this).apply {
                text = title
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.black))
            }

            val tvCompany = TextView(this).apply {
                text = "$company • $date"
                textSize = 14f
                setTextColor(resources.getColor(R.color.purple_primary))
                setPadding(0, 4, 0, 0)
            }

            val tvDesc = TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(resources.getColor(R.color.grey_text))
                setPadding(0, 8, 0, 0)
            }

            innerLayout.addView(tvTitle)
            innerLayout.addView(tvCompany)
            innerLayout.addView(tvDesc)

            cardView.addView(innerLayout)
            binding.llExperienceContainer.addView(cardView)
        }
    }

    private fun downloadResume(url: String) {
        try {
            var finalUrl = url
            if (url.startsWith("http://")) {
                finalUrl = url.replace("http://", "https://")
            }
            if (finalUrl.contains("/upload/")) {
                finalUrl = finalUrl.replace("/upload/", "/upload/fl_attachment/")
            }

            val request = DownloadManager.Request(Uri.parse(finalUrl))
                .setTitle("Applicant Resume")
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Applicant_CV_${System.currentTimeMillis()}.pdf")

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "Mengunduh Resume...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal Download: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}