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
import lat.pam.ydjob.databinding.ActivityEditCompanyProfileBinding
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class EditCompanyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCompanyProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedImageUri: Uri? = null
    private var currentPhotoUrl: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivProfilePreview.setImageURI(uri) // Tampilkan preview
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCompanyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCloudinary()

        binding.btnBack.setOnClickListener { finish() }

        binding.ivProfilePreview.setOnClickListener {
            pickImage.launch("image/*")
        }

        loadCurrentData()

        binding.btnSaveComp.setOnClickListener {
            validateAndSave()
        }
        setupDeleteButton()
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "......."
            config["api_key"] = "........"
            config["api_secret"] = ".........."
            MediaManager.init(this, config)
        } catch (e: Exception) {
        }
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etCompName.setText(document.getString("companyName"))
                    binding.etCompLoc.setText(document.getString("location"))
                    binding.etCompDesc.setText(document.getString("description"))
                    binding.etCompWeb.setText(document.getString("website"))
                    binding.etCompPhone.setText(document.getString("phone"))

                    currentPhotoUrl = document.getString("photoUrl") ?: ""
                    if (currentPhotoUrl.isNotEmpty()) {
                        Glide.with(this).load(currentPhotoUrl).into(binding.ivProfilePreview)
                    }
                }
            }
    }

    private fun validateAndSave() {
        val name = binding.etCompName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etCompName.error = "Company Name is required"
            return
        }

        binding.btnSaveComp.isEnabled = false
        binding.btnSaveComp.text = "Uploading..."

        if (selectedImageUri != null) {
            uploadPhotoToCloudinary(name)
        } else {
            saveDataToFirestore(currentPhotoUrl)
        }
    }

    private fun uploadPhotoToCloudinary(name: String) {
        val uri = selectedImageUri ?: return

        MediaManager.get().upload(uri)
            .unsigned("jobportal_preset")
            .option("folder", "company_logos")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val downloadUrl = resultData["secure_url"].toString()
                    saveDataToFirestore(downloadUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(this@EditCompanyProfileActivity, "Gagal upload foto: ${error.description}", Toast.LENGTH_SHORT).show()
                    binding.btnSaveComp.isEnabled = true
                    binding.btnSaveComp.text = "Save Changes"
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveDataToFirestore(photoUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        val newName = binding.etCompName.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "companyName" to newName,
            "location" to binding.etCompLoc.text.toString().trim(),
            "description" to binding.etCompDesc.text.toString().trim(),
            "website" to binding.etCompWeb.text.toString().trim(),
            "phone" to binding.etCompPhone.text.toString().trim(),
            "photoUrl" to photoUrl
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                updateAllPostedJobs(uid, newName, photoUrl)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                resetButtonState()
            }
    }

    private fun updateAllPostedJobs(companyId: String, newName: String, newLogoUrl: String) {
        binding.btnSaveComp.text = "Syncing Jobs..."

        db.collection("jobs")
            .whereEqualTo("companyId", companyId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    finishWithSuccess()
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                for (doc in documents) {
                    val jobRef = db.collection("jobs").document(doc.id)
                    batch.update(jobRef, "companyName", newName)
                    batch.update(jobRef, "companyLogo", newLogoUrl)
                }

                batch.commit().addOnSuccessListener {
                    finishWithSuccess()
                }.addOnFailureListener {
                    finishWithSuccess()
                }
            }
    }

    private fun finishWithSuccess() {
        Toast.makeText(this, "Profile & Jobs Updated!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetButtonState() {
        binding.btnSaveComp.isEnabled = true
        binding.btnSaveComp.text = "Save Changes"
    }

    private fun setupDeleteButton() {
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("You will be redirected to an account deletion form. Are you sure you want to proceed?")
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
