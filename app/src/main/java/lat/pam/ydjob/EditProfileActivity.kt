package lat.pam.ydjob

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityEditProfileBinding
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedImageUri: Uri? = null
    private var selectedCvUri: Uri? = null

    private var currentPhotoUrl: String = ""
    private var currentCvUrl: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivProfilePreview.setImageURI(uri)
        }
    }

    private val pickPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedCvUri = uri
            binding.tvCvName.text = "File Selected: PDF Document"
            binding.tvCvName.setTextColor(resources.getColor(R.color.purple_primary))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCloudinary()

        binding.btnBackEdit.setOnClickListener { finish() }

        binding.viewClickPhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.ivProfilePreview.setOnClickListener { pickImage.launch("image/*") }
        binding.btnPickCv.setOnClickListener { pickPdf.launch("application/pdf") }

        loadCurrentData()

        binding.btnSaveProfile.setOnClickListener {
            validateAndSave()
        }
        setupDeleteButton()
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        binding.btnSaveProfile.isEnabled = true
        binding.btnSaveProfile.text = "Save Changes"
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "........"
            config["api_key"] = ".........."
            config["api_secret"] = "......."
            MediaManager.init(this, config)
        } catch (e: Exception) {
        }
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etEditName.setText(document.getString("name"))
                    binding.etEditRole.setText(document.getString("jobTitle"))
                    binding.etEditBio.setText(document.getString("about"))
                    binding.etEditPhone.setText(document.getString("phoneNumber"))
                    binding.etEditLocation.setText(document.getString("location"))

                    currentPhotoUrl = document.getString("photoUrl") ?: ""
                    if (currentPhotoUrl.isNotEmpty()) {
                        Glide.with(this).load(currentPhotoUrl).into(binding.ivProfilePreview)
                    }

                    currentCvUrl = document.getString("resumeUrl") ?: ""
                    if (currentCvUrl.isNotEmpty()) {
                        binding.tvCvName.text = "Resume Already Uploaded"
                        binding.btnPickCv.text = "Change"
                    }
                }
            }
    }

    private fun validateAndSave() {
        val name = binding.etEditName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etEditName.error = "Name is required"
            return
        }

        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Saving..."

        uploadPhotoOrProceed()
    }

    private fun uploadPhotoOrProceed() {
        if (selectedImageUri != null) {
            binding.btnSaveProfile.text = "Uploading Photo..."
            MediaManager.get().upload(selectedImageUri)
                .unsigned("jobportal_preset")
                .option("folder", "profile_pics")
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        currentPhotoUrl = resultData["secure_url"].toString()
                        uploadCvOrProceed()
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        showError("Failed to upload photo: ${error.description}")
                    }
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } else {
            uploadCvOrProceed()
        }
    }

    private fun uploadCvOrProceed() {
        if (selectedCvUri != null) {
            binding.btnSaveProfile.text = "Uploading CV..."
            MediaManager.get()
                .upload(selectedCvUri)
                .unsigned("jobportal_preset")
                .option("folder", "resumes")
                .option("resource_type", "raw")
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        currentCvUrl = resultData["secure_url"].toString()
                        saveToFirestore()
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        showError("Upload CV failed: ${error.description}")
                    }

                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } else {
            saveToFirestore()
        }
    }


    private fun saveToFirestore() {
        binding.btnSaveProfile.text = "Finalizing..."
        val uid = auth.currentUser?.uid ?: return
        val newName = binding.etEditName.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "jobTitle" to binding.etEditRole.text.toString().trim(),
            "about" to binding.etEditBio.text.toString().trim(),
            "phoneNumber" to binding.etEditPhone.text.toString().trim(),
            "location" to binding.etEditLocation.text.toString().trim(),
            "photoUrl" to currentPhotoUrl,
            "resumeUrl" to currentCvUrl
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                updateAllApplications(uid, newName, currentPhotoUrl)
            }
            .addOnFailureListener {
                showError("Failed to update database: ${it.message}")
            }
    }

    private fun updateAllApplications(userId: String, newName: String, newPhotoUrl: String) {
        binding.btnSaveProfile.text = "Syncing Applications..."

        db.collection("applications")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    finishWithSuccess()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (doc in documents) {
                    val appRef = db.collection("applications").document(doc.id)
                    batch.update(appRef, "applicantName", newName)
                }

                batch.commit()
                    .addOnSuccessListener {
                        finishWithSuccess()
                    }
                    .addOnFailureListener { e ->
                        finishWithSuccess()
                    }
            }
            .addOnFailureListener {
                finishWithSuccess()
            }
    }

    private fun finishWithSuccess() {
        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getDownloadableUrl(url: String): String {
        return url.replace("/upload/", "/upload/fl_attachment/")
    }

    private fun setupDeleteButton() {
        binding.btnDeleteAccount.setOnClickListener {
            // Tampilkan dialog konfirmasi
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("You will be redirected to an account deletion form. This action is irreversible. Are you sure?")
                .setPositiveButton("Yes, Proceed") { _, _ ->
                    val url = "https://forms.gle/WijVxLcPkRTz1oy27"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}
