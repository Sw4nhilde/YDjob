package lat.pam.ydjob

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityApplyJobBinding
import lat.pam.ydjob.model.Job
import lat.pam.ydjob.repository.JobRepository
import lat.pam.ydjob.utils.UiState

class ApplyJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyJobBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repository = JobRepository()

    private var currentJob: Job? = null
    private var hasResume: Boolean = false
    private var resumeUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentJob = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_JOB", Job::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_JOB")
        }

        if (currentJob == null) {
            Toast.makeText(this, "Job Data Invalid. Cannot apply.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        setupListeners()
        fetchUserData()
    }

    private fun setupUI() {
        binding.tvJobTitle.text = currentJob?.title
        binding.tvCompanyName.text = currentJob?.companyName
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangeProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.btnSubmitApplication.setOnClickListener {
            if (!hasResume) {
                showResumeMissingDialog()
            } else {
                submitApplication()
            }
        }
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        setLoading(true)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                setLoading(false)
                if (doc != null && doc.exists()) {
                    binding.tvUserName.text = doc.getString("name")
                    val photoUrl = doc.getString("photoUrl")
                    resumeUrl = doc.getString("resumeUrl") ?: ""

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).circleCrop().into(binding.ivUserProfile)
                    }

                    if (resumeUrl.isNotEmpty()) {
                        hasResume = true
                        binding.tvResumeStatus.text = "Resume Attached"
                        binding.ivResumeStatus.setImageResource(R.drawable.ic_check_circle)
                        binding.ivResumeStatus.setColorFilter(
                            ContextCompat.getColor(this, android.R.color.holo_green_dark)
                        )
                    } else {
                        hasResume = false
                        binding.tvResumeStatus.text = "Resume Not Found"
                        binding.ivResumeStatus.setImageResource(R.drawable.ic_warning)
                        binding.ivResumeStatus.setColorFilter(
                            ContextCompat.getColor(this, android.R.color.holo_red_dark)
                        )
                    }
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitApplication() {
        val job = currentJob ?: return
        val coverLetter = binding.etCoverLetter.text.toString().trim()

        if (job.id.isNullOrEmpty()) {
            Toast.makeText(this, "Cannot apply: Job ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        if (resumeUrl.isEmpty()) {
            showResumeMissingDialog()
            return
        }

        setLoading(true)
        binding.btnSubmitApplication.text = "Submitting..."

        repository.applyJob(job, coverLetter, resumeUrl) { state ->
            when (state) {
                is UiState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, state.data, Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                is UiState.Failure -> {
                    setLoading(false)
                    binding.btnSubmitApplication.text = "Submit Application"
                    Toast.makeText(this, "Failed: ${state.error}", Toast.LENGTH_SHORT).show()
                }
                is UiState.Loading -> { }
            }
        }
    }

    private fun showResumeMissingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Resume Missing")
            .setMessage("You must upload a CV/Resume first.")
            .setPositiveButton("Go to Profile") { _, _ ->
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitApplication.isEnabled = !isLoading
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser?.uid != null) {
            fetchUserData()
        }
    }
}