package lat.pam.ydjob.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import lat.pam.ydjob.repository.AuthRepository
import lat.pam.ydjob.utils.UiState

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<UiState<FirebaseUser>>()
    val loginState: LiveData<UiState<FirebaseUser>> = _loginState

    private val _registerState = MutableLiveData<UiState<String>>()
    val registerState: LiveData<UiState<String>> = _registerState

    fun login(email: String, pass: String) {
        _loginState.value = UiState.Loading
        repository.login(email, pass) { result ->
            _loginState.value = result
        }
    }

    fun register(email: String, pass: String, name: String, role: String) {
        _registerState.value = UiState.Loading
        repository.register(email, pass, name, role) { result ->
            _registerState.value = result
        }
    }
}