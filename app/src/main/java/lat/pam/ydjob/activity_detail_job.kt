package lat.pam.ydjob

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import lat.pam.ydjob.databinding.ActivityDetailJobBinding
import lat.pam.ydjob.model.Job

class DetailJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailJobBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackDetail.setOnClickListener { finish() }

        // Mengambil objek Job dari Intent dengan cara yang aman untuk versi Android terbaru
        val job = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_JOB", Job::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_JOB")
        }

        if (job != null) {
            setupData(job)
        } else {
            Toast.makeText(this, "Error: Data pekerjaan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupData(job: Job) {
        binding.tvDetailTitle.text = job.title
        binding.tvDetailCompany.text = job.companyName
        binding.tvDetailDesc.text = job.description?.replace("\\n", "\n")
        binding.tvDetailLocation.text = job.location
        binding.tvDetailSalary.text = "Rp ${formatK(job.salaryMin)} - ${formatK(job.salaryMax)}"
        binding.tvTag1.text = job.jobType
        binding.tvTag2.text = job.workplace

        if (job.companyLogo.isNotEmpty()) {
            Glide.with(this)
                .load(job.companyLogo)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.ivDetailLogo)
        }

        fun openCompanyDetail() {
            if (job.companyId.isNotEmpty()) {
                val intent = Intent(this, CompanyDetailActivity::class.java)
                intent.putExtra("EXTRA_COMPANY_ID", job.companyId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Informasi perusahaan tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvDetailCompany.setOnClickListener { openCompanyDetail() }
        binding.ivDetailLogo.setOnClickListener { openCompanyDetail() }

        binding.btnApply.setOnClickListener {
            val intent = Intent(this, ApplyJobActivity::class.java)

            intent.putExtra("EXTRA_JOB", job)

            startActivity(intent)
        }
    }

    private fun formatK(value: Long): String {
        return when {
            value >= 1000000 -> "${value / 1000000}jt"
            value >= 1000 -> "${value / 1000}k"
            else -> value.toString()
        }
    }
}
