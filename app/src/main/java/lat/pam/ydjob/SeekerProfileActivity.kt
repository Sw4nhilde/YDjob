package lat.pam.ydjob

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivitySeekerProfileBinding

class SeekerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeekerProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentResumeUrl = ""
    private var currentPhotoUrl = ""

    private var fullExperienceList: List<Map<String, String>> = listOf()
    private var isExperienceExpanded = false

    private val TAG = "SeekerProfile"

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) uploadProfilePhoto(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCloudinary()
        fetchUserProfile()
        setupListeners()
    }

    private fun initCloudinary() {
        try {
            val config = hashMapOf(
                "cloud_name" to "dlu912m",
                "api_key" to "975736617",
                "api_secret" to "o7w-v9"
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {
            Log.w(TAG, "Cloudinary already initialized")
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            auth.signOut()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }

        // Edit Profile
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Ganti Foto Cepat
        binding.cvProfilePic.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Download Resume
        binding.cvResume.setOnClickListener {
            if (currentResumeUrl.isNotEmpty()) {
                downloadResume(currentResumeUrl)
            } else {
                Toast.makeText(this, "Resume belum diupload", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        }

        binding.btnAddSkill.setOnClickListener { showAddSkillDialog() }

        binding.btnAddExperience.setOnClickListener { showAddExperienceDialog() }

        binding.tvViewAllExperience.setOnClickListener {
            isExperienceExpanded = !isExperienceExpanded
            renderExperienceList()
        }
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                binding.tvName.text = doc.getString("name") ?: "No Name"
                binding.tvRole.text = doc.getString("jobTitle") ?: "Job Seeker"
                binding.tvBio.text = doc.getString("about") ?: "No bio available."

                currentPhotoUrl = doc.getString("photoUrl") ?: ""
                currentResumeUrl = doc.getString("resumeUrl") ?: ""


                if (currentPhotoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(currentPhotoUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.ivProfile)
                }

                binding.tvResumeName.text =
                    if (currentResumeUrl.isNotEmpty()) "Tap to Download PDF"
                    else "No Resume (Tap to Edit)"

                if (currentResumeUrl.isNotEmpty()) {
                    binding.tvResumeName.setTextColor(resources.getColor(android.R.color.white))
                }

                (doc.get("skills") as? List<String>)?.let { populateSkills(it) }

                val rawExp = doc.get("experiences") as? List<Map<String, String>>
                fullExperienceList = rawExp ?: listOf()
                renderExperienceList()
            }
            .addOnFailureListener {
                Log.e(TAG, "Error fetching profile: ${it.message}")
            }
    }

    private fun renderExperienceList() {
        binding.llExperienceContainer.removeAllViews()

        val displayList = if (isExperienceExpanded) {
            fullExperienceList
        } else {
            fullExperienceList.take(3)
        }

        for (exp in displayList) {
            val title = exp["title"] ?: ""
            val company = exp["company"] ?: ""
            val date = exp["date"] ?: ""
            val desc = exp["description"] ?: ""

            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 24) }
                radius = 16f
                cardElevation = 4f
                setContentPadding(32, 32, 32, 32)
                setCardBackgroundColor(resources.getColor(android.R.color.white))
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
                text = company
                textSize = 14f
                setTextColor(resources.getColor(R.color.purple_primary))
                setPadding(0, 4, 0, 0)
            }
            val tvDate = TextView(this).apply {
                text = date
                textSize = 12f
                setTextColor(resources.getColor(R.color.grey_text))
                setPadding(0, 8, 0, 0)
            }
            val tvDesc = TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(resources.getColor(R.color.black))
                setPadding(0, 16, 0, 0)
            }

            innerLayout.addView(tvTitle)
            innerLayout.addView(tvCompany)
            innerLayout.addView(tvDate)
            innerLayout.addView(tvDesc)

            cardView.addView(innerLayout)
            binding.llExperienceContainer.addView(cardView)
        }

        if (fullExperienceList.size > 3) {
            binding.tvViewAllExperience.visibility = View.VISIBLE
            if (isExperienceExpanded) {
                binding.tvViewAllExperience.text = "Show Less"
            } else {
                binding.tvViewAllExperience.text = "View All (${fullExperienceList.size - 3} more)"
            }
        } else {
            binding.tvViewAllExperience.visibility = View.GONE
        }
    }

    private fun populateSkills(skills: List<String>) {
        binding.chipGroupSkills.removeAllViews()
        skills.forEach {
            val chip = Chip(this)
            chip.text = it
            chip.setChipBackgroundColorResource(com.google.android.material.R.color.m3_ref_palette_dynamic_neutral95)
            binding.chipGroupSkills.addView(chip)
        }
    }

    private fun downloadResume(url: String) {
        try {
            var finalUrl = url
            if (url.startsWith("http:")) {
                finalUrl = url.replace("http:", "https:")
            }
            if (finalUrl.contains("/upload/")) {
                finalUrl = finalUrl.replace("/upload/", "/upload/fl_attachment/")
            }

            Log.d(TAG, "Downloading from: $finalUrl")
            Toast.makeText(this, "Downloading resume...", Toast.LENGTH_SHORT).show()

            val request = DownloadManager.Request(Uri.parse(finalUrl))
                .setTitle("My Resume")
                .setDescription("Downloading PDF...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "Resume_${System.currentTimeMillis()}.pdf"
                )

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddSkillDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add Skill")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val skill = input.text.toString().trim()
                if (skill.isNotEmpty()) {
                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .update("skills", FieldValue.arrayUnion(skill))
                        .addOnSuccessListener { fetchUserProfile() }
                }
            }.show()
    }

    private fun showAddExperienceDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etTitle = EditText(this).apply { hint = "Job Title" }
        val etCompany = EditText(this).apply { hint = "Company" }
        val etDate = EditText(this).apply { hint = "Period (e.g. 2020-2023)" }
        val etDesc = EditText(this).apply {
            hint = "Description"
            minLines = 3
            gravity = android.view.Gravity.TOP
        }

        layout.addView(etTitle)
        layout.addView(etCompany)
        layout.addView(etDate)
        layout.addView(etDesc)

        AlertDialog.Builder(this)
            .setTitle("Add Experience")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val company = etCompany.text.toString().trim()
                val date = etDate.text.toString().trim()
                val desc = etDesc.text.toString().trim()

                if (title.isNotEmpty() && company.isNotEmpty()) {
                    saveExperienceToFirestore(title, company, date, desc)
                } else {
                    Toast.makeText(this, "Title & Company Required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveExperienceToFirestore(title: String, company: String, date: String, desc: String) {
        val uid = auth.currentUser?.uid ?: return
        val newExp = hashMapOf(
            "title" to title,
            "company" to company,
            "date" to date,
            "description" to desc
        )
        db.collection("users").document(uid)
            .update("experiences", FieldValue.arrayUnion(newExp))
            .addOnSuccessListener {
                Toast.makeText(this, "Experience Added!", Toast.LENGTH_SHORT).show()
                fetchUserProfile() // Refresh UI agar data baru muncul
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show()

        MediaManager.get()
            .upload(uri)
            .unsigned("jobportal_preset")
            .option("folder", "profile_pics")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"].toString()
                    db.collection("users").document(uid)
                        .update("photoUrl", url)
                        .addOnSuccessListener { fetchUserProfile() }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(this@SeekerProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }

                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }
}