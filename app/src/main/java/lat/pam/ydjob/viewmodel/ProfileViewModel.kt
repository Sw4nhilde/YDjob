package lat.pam.ydjob.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import lat.pam.ydjob.repository.ProfileRepository
import lat.pam.ydjob.utils.UiState


class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _updateState = MutableLiveData<UiState<String>>()
    val updateState: LiveData<UiState<String>> = _updateState

    private val _roleState = MutableLiveData<UiState<String>>()
    val roleState: LiveData<UiState<String>> = _roleState

    fun fetchUserRole() {
        _roleState.value = UiState.Loading
        repository.getUserRole { result ->
            _roleState.value = result
        }
    }

    fun updateCompany(name: String, desc: String, loc: String) {
        _updateState.value = UiState.Loading
        repository.updateCompanyProfile(name, desc, loc) { result ->
            _updateState.value = result
        }
    }

    fun updateSeeker(skills: String, exp: String) {
        _updateState.value = UiState.Loading
        repository.updateSeekerProfile(skills, exp) { result ->
            _updateState.value = result
        }
    }
}