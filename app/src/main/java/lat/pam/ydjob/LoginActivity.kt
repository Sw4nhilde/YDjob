package lat.pam.ydjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.databinding.ActivityLoginBinding
import lat.pam.ydjob.utils.UiState
import lat.pam.ydjob.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            checkRoleAndNavigate(currentUser.uid)
        }

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupAction()
        setupObserver()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.login(email, pass)
            } else {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur Reset Password belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObserver() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is UiState.Success -> {
                    val userId = state.data.uid
                    Toast.makeText(this, "Login Berhasil, memuat data...", Toast.LENGTH_SHORT).show()

                    checkRoleAndNavigate(userId)
                }
                is UiState.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Error: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkRoleAndNavigate(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (document.exists()) {
                    val role = document.getString("role")

                    val targetActivity = if (role == "company") {
                        CompanyDashboardActivity::class.java
                    } else {
                        MainActivity::class.java
                    }

                    val intent = Intent(this, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Data user tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}