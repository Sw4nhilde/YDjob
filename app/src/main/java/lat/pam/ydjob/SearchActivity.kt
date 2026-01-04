package lat.pam.ydjob

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import lat.pam.ydjob.adapter.JobAdapter
import lat.pam.ydjob.databinding.ActivitySearchBinding
import lat.pam.ydjob.utils.UiState
import lat.pam.ydjob.viewmodel.JobViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: JobViewModel
    private lateinit var adapter: JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[JobViewModel::class.java]

        setupRecyclerView()
        setupSearchListener()
        setupFilterChips()
        setupObserver()

        viewModel.fetchJobsForSearch()
    }

    private fun setupRecyclerView() {
        adapter = JobAdapter { job ->
            val intent = Intent(this, DetailJobActivity::class.java)
            intent.putExtra("EXTRA_JOB", job)
            startActivity(intent)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = adapter
    }

    private fun setupSearchListener() {
        binding.etSearchQuery.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.etSearchQuery.right - binding.etSearchQuery.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    binding.etSearchQuery.text.clear()
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterJobs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.etSearchQuery.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchQuery.text.toString()
                viewModel.filterJobs(query)
                true
            } else false
        }
    }

    private fun setupFilterChips() {
        binding.chipFulltime.setOnClickListener {
            viewModel.filterByCategory("Full Time")
            binding.etSearchQuery.text.clear()
        }
        binding.chipRemote.setOnClickListener {
            viewModel.filterByCategory("Remote")
            binding.etSearchQuery.text.clear()
        }
        binding.chipContract.setOnClickListener {
            viewModel.filterByCategory("Contract")
            binding.etSearchQuery.text.clear()
        }
    }

    private fun setupObserver() {
        viewModel.searchResult.observe(this) { jobs ->
            adapter.setData(jobs)

            binding.tvFound.text = "${jobs.size} Job founded"

            if (jobs.isEmpty()) {
            }
        }


        viewModel.jobsState.observe(this) { state ->
            if (state is UiState.Loading) {
                binding.progressBarSearch.visibility = View.VISIBLE
            } else {
                binding.progressBarSearch.visibility = View.GONE
            }
        }
    }
}