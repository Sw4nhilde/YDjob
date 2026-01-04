package lat.pam.ydjob

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.adapter.ApplicantAdapter
import lat.pam.ydjob.databinding.ActivityApplicantsBinding
import lat.pam.ydjob.model.Application

class ApplicantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicantsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val applicantsList = ArrayList<Application>()
    private lateinit var adapter: ApplicantAdapter
    private var jobId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jobId = intent.getStringExtra("EXTRA_JOB_ID") ?: ""

        if (jobId.isEmpty()) {
            Toast.makeText(this, "Job ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        fetchApplicants()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicantAdapter(applicantsList, this)

        binding.rvApplicants.apply {
            layoutManager = LinearLayoutManager(this@ApplicantsActivity)
            adapter = this@ApplicantsActivity.adapter
        }
    }

    private fun fetchApplicants() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("companyId", uid)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                applicantsList.clear()

                if (documents.isEmpty) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvApplicants.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvApplicants.visibility = View.VISIBLE

                    for (doc in documents) {
                        try {
                            val app = doc.toObject(Application::class.java)

                            applicantsList.add(app)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}