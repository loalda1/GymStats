package org.gymstats.android.ui
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.lifecycle.*
import androidx.fragment.app.viewModels
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.gymstats.android.R
import org.gymstats.android.databinding.FragmentAuthBinding
class AuthViewModel : ViewModel() {
    val busy = MutableStateFlow(false)
    val result = MutableStateFlow<Int?>(null)
    var registering = false
    fun run(action: suspend () -> Int) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { result.value = action() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { result.value = R.string.auth_failed }
            finally { busy.value = false }
        }
    }
}
class AuthFragment : BindingFragment<FragmentAuthBinding>(FragmentAuthBinding::inflate) {
    private val authState: AuthViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.status.visibility = if (gymHost.auth == null) View.VISIBLE else View.GONE
        fun render() { binding.submit.setText(if (authState.registering) R.string.register else R.string.login); binding.toggle.setText(if (authState.registering) R.string.back_login else R.string.register) }
        render(); binding.toggle.setOnClickListener { authState.registering = !authState.registering; render() }
        binding.demo.setOnClickListener { gymHost.startDemo() }
        binding.submit.setOnClickListener {
            val email = binding.email.text.toString().trim(); val password = binding.password.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.length < 6) { message(R.string.invalid_auth); return@setOnClickListener }
            val auth = gymHost.auth ?: run { message(R.string.cloud_missing); return@setOnClickListener }
            val register = authState.registering
            authState.run { if (register) auth.register(email, password) else auth.login(email, password); 0 }
        }
        binding.reset.setOnClickListener {
            val email = binding.email.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { message(R.string.invalid_auth); return@setOnClickListener }
            val auth = gymHost.auth ?: run { message(R.string.cloud_missing); return@setOnClickListener }
            authState.run { auth.reset(email); R.string.reset_sent }
        }
        binding.google.setOnClickListener {
            val auth = gymHost.auth ?: run { message(R.string.cloud_missing); return@setOnClickListener }
            val clientId = getString(R.string.google_web_client_id)
            if (clientId.isBlank()) { message(R.string.cloud_missing); return@setOnClickListener }
            // A Google credential request belongs to the currently visible Activity.
            viewLifecycleOwner.lifecycleScope.launch {
                if (authState.busy.value) return@launch
                authState.busy.value = true
                var handedOff = false
                try {
                    val option = GetSignInWithGoogleOption.Builder(clientId).build()
                    val result = CredentialManager.create(requireContext()).getCredential(requireActivity(), GetCredentialRequest.Builder().addCredentialOption(option).build())
                    val credential = result.credential
                    require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    authState.busy.value = false
                    authState.run { auth.google(token); 0 }; handedOff = true
                } catch (_: GetCredentialCancellationException) { authState.result.value = R.string.cancelled }
                catch (_: NoCredentialException) { authState.result.value = R.string.auth_failed }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { authState.result.value = R.string.auth_failed }
                finally { if (!handedOff) authState.busy.value = false }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch { viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { authState.busy.collect { busy -> binding.progress.visibility = if (busy) View.VISIBLE else View.GONE; listOf(binding.submit, binding.google, binding.toggle, binding.reset, binding.demo).forEach { it.isEnabled = !busy } } }
            launch { authState.result.collect { result -> if (result != null) { authState.result.value = null; if (result == 0) gymHost.authenticated() else message(result) } } }
        } }
    }
}
