package lat.pam.ydjob

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import lat.pam.ydjob.databinding.ActivityRegisterBinding
import lat.pam.ydjob.utils.UiState
import lat.pam.ydjob.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupAction()
        setupObserver()
    }

    private fun setupAction() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val pass = binding.etPassReg.text.toString().trim()

            val role = if (binding.rbCompany.isChecked) "company" else "seeker"

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.register(email, pass, name, role)
            } else {
                Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoginRedirect.setOnClickListener {
            finish()
        }
    }

    private fun setupObserver() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarReg.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBarReg.visibility = View.GONE
                    Toast.makeText(this, state.data, Toast.LENGTH_LONG).show()

                    finish()
                }
                is UiState.Failure -> {
                    binding.progressBarReg.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Gagal: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}