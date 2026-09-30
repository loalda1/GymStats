package com.example.gymstats.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gymstats.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    val account = task.result
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Sesión iniciada con Google",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFragment
                            )
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                requireContext(),
                                "Error con Firebase: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error con Google: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Inicio de sesión cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val editTextEmail = view.findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = view.findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val buttonGoogleSignIn = view.findViewById<Button>(R.id.buttonGoogleSignIn)
        val buttonGoToRegister = view.findViewById<Button>(R.id.buttonGoToRegister)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Rellena email y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Sesión iniciada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(
                        R.id.action_loginFragment_to_homeFragment
                    )
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        buttonGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        buttonGoToRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_registerFragment
            )
        }
    }

    private fun signInWithGoogle() {
        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
}