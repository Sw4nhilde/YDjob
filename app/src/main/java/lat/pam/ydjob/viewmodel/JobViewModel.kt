package lat.pam.ydjob.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration // Import ini penting
import lat.pam.ydjob.model.Application
import lat.pam.ydjob.model.Job
import lat.pam.ydjob.repository.JobRepository
import lat.pam.ydjob.utils.UiState

class JobViewModel : ViewModel() {

    private val repository = JobRepository()

    private var listenerRegistration: ListenerRegistration? = null

    private val _jobsState = MutableLiveData<UiState<List<Job>>>()
    val jobsState: LiveData<UiState<List<Job>>> = _jobsState

    private val _addJobState = MutableLiveData<UiState<String>>()
    val addJobState: LiveData<UiState<String>> = _addJobState

    private val _applicantState = MutableLiveData<UiState<List<Application>>>()
    val applicantState: LiveData<UiState<List<Application>>> = _applicantState

    private var originalJobList = listOf<Job>()
    private val _searchResult = MutableLiveData<List<Job>>()
    val searchResult: LiveData<List<Job>> = _searchResult

    fun fetchJobsRealtime() {
        _jobsState.value = UiState.Loading

        listenerRegistration?.remove()

        listenerRegistration = repository.getJobsRealtime { result ->

            _jobsState.value = result

            if (result is UiState.Success) {
                originalJobList = result.data
                if (_searchResult.value.isNullOrEmpty() || _searchResult.value?.size == originalJobList.size) {
                    _searchResult.value = originalJobList
                }
            }
        }
    }

    fun postJob(
        title: String,
        loc: String,
        min: String,
        max: String,
        desc: String,
        type: String
    ) {
        _addJobState.value = UiState.Loading

        val salaryMin = min.toLongOrNull() ?: 0L
        val salaryMax = max.toLongOrNull() ?: 0L

        repository.addJob(title, loc, salaryMin, salaryMax, desc, type) { result ->
            _addJobState.value = result
        }
    }

    fun fetchCompanyApplications() {
        _applicantState.value = UiState.Loading
        repository.getApplicationsByCompany { result ->
            _applicantState.value = result
        }
    }

    fun fetchJobsForSearch() {
        _jobsState.value = UiState.Loading
        repository.getAllJobs { result ->
            if (result is UiState.Success) {
                originalJobList = result.data
                _searchResult.value = originalJobList
                _jobsState.value = UiState.Success(originalJobList)
            } else {
                _jobsState.value = result
            }
        }
    }

    fun filterJobs(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalJobList
        } else {
            originalJobList.filter { job ->
                job.title.contains(query, ignoreCase = true) ||
                        job.location.contains(query, ignoreCase = true)
            }
        }
        _searchResult.value = filteredList
    }

    fun filterByCategory(category: String) {
        val filteredList = originalJobList.filter { job ->
            job.jobType.contains(category, ignoreCase = true)
        }
        _searchResult.value = filteredList
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}