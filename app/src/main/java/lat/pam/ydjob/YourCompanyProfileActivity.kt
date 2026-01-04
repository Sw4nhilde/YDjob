package lat.pam.ydjob

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityYourCompanyProfileBinding

class YourCompanyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYourCompanyProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourCompanyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchMyProfile()

        binding.btnLogoutCompany.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnEditCompany.setOnClickListener {
            val intent = Intent(this, EditCompanyProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchMyProfile()
    }

    private fun fetchMyProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("companyName") ?: document.getString("name") ?: "No Name"
                    val location = document.getString("location") ?: ""
                    val desc = document.getString("description") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val email = document.getString("email") ?: auth.currentUser?.email ?: ""
                    val website = document.getString("website") ?: ""
                    val photoUrl = document.getString("photoUrl")

                    binding.tvCompanyName.text = name
                    binding.tvCompanyLoc.text = location
                    binding.tvAbout.text = desc
                    binding.tvPhone.text = phone
                    binding.tvEmail.text = email
                    binding.tvWebsite.text = website

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivCompanyLogo)
                    }
                }
            }
    }
}