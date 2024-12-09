package com.videomate.critix.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.videomate.critix.databinding.ActivityRegisterBinding
import com.videomate.critix.model.RegisterRequest
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ApiService and UserRepository
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)

        // Use ViewModelFactory to initialize the ViewModel
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        setOnClick()
        observeViewModel()
    }

    private fun setOnClick() {
        binding.registerButton.setOnClickListener {
            if (validateForm()) {
                val username = binding.username.text.toString().trim()
                val email = binding.email.text.toString().trim()
                val password = binding.password.text.toString().trim()

                // Call the ViewModel to perform the registration
                val request = RegisterRequest(username, email, password)
                userViewModel.registerUser(request)
            }
        }

        binding.loginRedirect.setOnClickListener {
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun observeViewModel() {
        // Observe the registration response from ViewModel
        userViewModel.registerResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.success) {
                        it.data?.let { it1 ->
                            SharedPrefManager.saveUserData(this@RegisterActivity,
                                it1.token,it.data.userId,it.data.username)
                        }
                        Toast.makeText(this, "Registration Successful! Welcome ${it.data?.username}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed: Email already exist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        val username = binding.username.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            binding.username.error = "Username is required"
            return false
        }

        if (TextUtils.isEmpty(email)) {
            binding.email.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Invalid email address"
            return false
        }

        if (TextUtils.isEmpty(password)) {
            binding.password.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.password.error = "Password must be at least 6 characters"
            return false
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPassword.error = "Please confirm your password"
            return false
        } else if (confirmPassword != password) {
            binding.confirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }
}
