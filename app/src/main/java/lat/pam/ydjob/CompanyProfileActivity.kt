package lat.pam.ydjob

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import lat.pam.ydjob.databinding.ActivityCompanyProfileBinding
import lat.pam.ydjob.utils.UiState
import lat.pam.ydjob.viewmodel.ProfileViewModel


class CompanyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupAction()
        setupObserver()
    }

    private fun setupAction() {
        binding.btnSaveComp.setOnClickListener {
            val name = binding.etCompName.text.toString()
            val location = binding.etCompLoc.text.toString()
            val desc = binding.etCompDesc.text.toString()

            if (name.isNotEmpty() && location.isNotEmpty() && desc.isNotEmpty()) {
                // Panggil fungsi khusus Perusahaan
                viewModel.updateCompany(name, desc, location)
            } else {
                Toast.makeText(this, "Mohon lengkapi data perusahaan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObserver() {
        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarComp.visibility = View.VISIBLE
                    binding.btnSaveComp.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBarComp.visibility = View.GONE
                    binding.btnSaveComp.isEnabled = true
                    Toast.makeText(this, state.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is UiState.Failure -> {
                    binding.progressBarComp.visibility = View.GONE
                    binding.btnSaveComp.isEnabled = true
                    Toast.makeText(this, "Gagal: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}