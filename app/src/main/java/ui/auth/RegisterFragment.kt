package com.example.gymstats.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gymstats.R
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val editTextEmail = view.findViewById<EditText>(R.id.editTextRegisterEmail)
        val editTextPassword = view.findViewById<EditText>(R.id.editTextRegisterPassword)
        val buttonRegister = view.findViewById<Button>(R.id.buttonRegister)
        val buttonBackToLogin = view.findViewById<Button>(R.id.buttonBackToLogin)

        buttonRegister.setOnClickListener {
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

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Cuenta creada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(
                        R.id.action_registerFragment_to_homeFragment
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

        buttonBackToLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_registerFragment_to_loginFragment
            )
        }
    }
}