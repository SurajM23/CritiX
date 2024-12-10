package com.videomate.critix.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.ActivityLoginBinding
import com.videomate.critix.model.LoginRequest
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkLogin()
        initView()
        setOnClick()
        observeViewModel()
    }

    private fun initView() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun checkLogin() {
        val username = SharedPrefManager.getUsername(this@LoginActivity)
        val lastLoginTime = SharedPrefManager.getLastLoginTime(this@LoginActivity)
        val twelveHoursInMillis = 12 * 60 * 60 * 1000
        val currentTime = System.currentTimeMillis()
        if (!username.isNullOrEmpty() && currentTime < lastLoginTime + twelveHoursInMillis) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun setOnClick() {
        binding.loginButton.setOnClickListener {
            if (validateForm()) {
                val email = binding.email.text.toString().trim()
                val password = binding.password.text.toString().trim()
                val request = LoginRequest(email, password)
                userViewModel.loginUser(request)
            }
        }
        binding.registerRedirect.setOnClickListener {
            finish()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        userViewModel.loginResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.success) {
                        it.data?.let { userData ->
                            // Save the user data (token, userId, username)
                            SharedPrefManager.saveUserData(
                                this@LoginActivity,
                                userData.token,
                                userData.userId,
                                userData.username
                            )

                            // Save the current time as the last login time (in milliseconds)
                            val currentTime = System.currentTimeMillis()
                            val sharedPreferences = getSharedPreferences("UserPref", MODE_PRIVATE)
                            sharedPreferences.edit().apply {
                                putLong("lastLoginTime", currentTime)  // Store the current time as the last login time
                                apply()
                            }

                            // Show a welcome message
                            Toast.makeText(this, "Welcome, ${userData.username}!", Toast.LENGTH_SHORT).show()

                            // Redirect to HomeActivity
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Error: Email not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        if (email.isEmpty()) {
            binding.email.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Invalid email"
            return false
        }
        if (password.isEmpty()) {
            binding.password.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.password.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }
}

