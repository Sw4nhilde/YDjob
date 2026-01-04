package lat.pam.ydjob.utils

sealed class UiState<out T> {
    data class Success<out T>(val data: T) : UiState<T>()
    data class Failure(val error: String?) : UiState<Nothing>()
    object Loading : UiState<Nothing>()
}