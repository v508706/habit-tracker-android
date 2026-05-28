package com.habittracker.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.habittracker.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val setupDone: Boolean) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).await()

                val uid = auth.currentUser?.uid ?: throw Exception("No user after sign-in")
                repository.syncFromFirestore(uid)
                val profile = repository.getProfile()
                _state.value = LoginState.Success(setupDone = profile?.setupDone == true)
            } catch (e: ApiException) {
                _state.value = LoginState.Error(
                    when (e.statusCode) {
                        12501 -> "Sign-in cancelled. Please try again."
                        7 -> "Network error. Check your connection."
                        else -> "Sign-in failed. Please try again."
                    }
                )
            } catch (e: Exception) {
                _state.value = LoginState.Error("Sign-in failed. Please try again.")
            }
        }
    }

    fun resetState() { _state.value = LoginState.Idle }
}
