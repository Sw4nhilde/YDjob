package lat.pam.ydjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lat.pam.ydjob.adapter.JobAdapter
import lat.pam.ydjob.adapter.RecommendedJobAdapter
import lat.pam.ydjob.databinding.ActivityMainBinding
import lat.pam.ydjob.utils.UiState
import lat.pam.ydjob.viewmodel.JobViewModel
import lat.pam.ydjob.viewmodel.ProfileViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var jobViewModel: JobViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var recentJobAdapter: JobAdapter
    private lateinit var recommendedAdapter: RecommendedJobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        jobViewModel = ViewModelProvider(this)[JobViewModel::class.java]

        setupRecyclerViews()
        setupBottomNav()
        setupObservers()

        profileViewModel.fetchUserRole()
        jobViewModel.fetchJobsRealtime()
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: document.getString("companyName") ?: "User"
                    val photoUrl = document.getString("photoUrl")

                    binding.tvUsername.text = name


                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.ivLogo)
                    }
                }
            }
    }

    private fun setupRecyclerViews() {
        recentJobAdapter = JobAdapter { job ->
            val intent = Intent(this, DetailJobActivity::class.java)
            intent.putExtra("EXTRA_JOB", job)
            startActivity(intent)
        }
        binding.rvRecentJobs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recentJobAdapter
            isNestedScrollingEnabled = false
        }

        binding.rvRecommended.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupBottomNav() {
        binding.etSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
        binding.etSearch.isFocusable = false
        binding.etSearch.isClickable = true

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToProfile() {
        val currentState = profileViewModel.roleState.value
        if (currentState is UiState.Success) {
            val role = currentState.data
            if (role == "company") {
                startActivity(Intent(this, CompanyProfileActivity::class.java))
            } else {
                startActivity(Intent(this, SeekerProfileActivity::class.java))
            }
        } else {
            startActivity(Intent(this, SeekerProfileActivity::class.java))
        }
    }

    private fun setupObservers() {
        jobViewModel.jobsState.observe(this) { state ->
            when(state) {
                is UiState.Loading -> {
                }
                is UiState.Success -> {
                    val allJobs = state.data

                    recentJobAdapter.setData(allJobs)

                    recommendedAdapter = RecommendedJobAdapter(allJobs.take(5)) { job ->
                        val intent = Intent(this, DetailJobActivity::class.java)
                        intent.putExtra("EXTRA_JOB", job)
                        startActivity(intent)
                    }
                    binding.rvRecommended.adapter = recommendedAdapter
                }
                is UiState.Failure -> {
                    Toast.makeText(this, "Gagal memuat: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        profileViewModel.roleState.observe(this) { state ->
            if (state is UiState.Success) {
                val role = state.data
                if (role == "company") {
                    binding.tvGreeting.text = "Company Dashboard"
                    binding.tvUsername.setOnClickListener {
                        startActivity(Intent(this, CompanyProfileActivity::class.java))
                    }
                } else {
                    binding.tvGreeting.text = "Good Morning,"
                    binding.tvUsername.setOnClickListener {
                        startActivity(Intent(this, SeekerProfileActivity::class.java))
                    }
                }
            }
        }
    }
}