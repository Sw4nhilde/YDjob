package lat.pam.ydjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.adapter.CompanyJobAdapter // (Kita buat adapter ini di langkah 4)
import lat.pam.ydjob.databinding.ActivityCompanyDashboardBinding
import lat.pam.ydjob.model.Job

class CompanyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDashboardBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: CompanyJobAdapter
    private val jobList = ArrayList<Job>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        fetchCompanyProfile()
        fetchMyJobs()

        binding.tvCompanyName.setOnClickListener {
            startActivity(Intent(this, YourCompanyProfileActivity::class.java))
        }
        binding.fabAddJob.setOnClickListener {
            startActivity(Intent(this, AddJobActivity::class.java))
        }
    }

    private fun setupUI() {
        adapter = CompanyJobAdapter(jobList) { job ->
            val intent = Intent(this, ApplicantsActivity::class.java)
            intent.putExtra("EXTRA_JOB_ID", job.id)
            startActivity(intent)
        }
        binding.rvCompanyJobs.layoutManager = LinearLayoutManager(this)
        binding.rvCompanyJobs.adapter = adapter


    }

    private fun fetchCompanyProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("companyName") ?: "Company"
                    binding.tvCompanyName.text = name
                }
            }
    }

    private fun fetchMyJobs() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("jobs")
            .whereEqualTo("companyId", uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading jobs", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    jobList.clear()
                    for (doc in value) {
                        val job = doc.toObject(Job::class.java)
                        jobList.add(job)
                    }
                    adapter.notifyDataSetChanged()

                    binding.tvStatJobs.text = jobList.size.toString()
                }
            }
    }
}