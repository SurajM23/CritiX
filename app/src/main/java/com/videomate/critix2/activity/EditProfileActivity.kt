package com.videomate.critix2.activity

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix.databinding.ActivityEditProfileBinding
import com.videomate.critix2.apiService.ApiServiceBuilder
import com.videomate.critix2.model.UpdateUserRequest
import com.videomate.critix2.model.UserDetails
import com.videomate.critix2.repository.UserRepository
import com.videomate.critix2.utils.GlobelMethods
import com.videomate.critix2.utils.SharedPrefManager
import com.videomate.critix2.viewModel.UserViewModel
import com.videomate.critix2.viewModel.UserViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userViewModel: UserViewModel
    private var selectedImageUri: Uri? = null
    private val imagePickerResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Picasso.get().load(uri).into(binding.imgProfile)
            Toast.makeText(this, "Image selected: $uri", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to pick an image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setOnClick()
        setObserver()
    }



    private fun setObserver() {
        userViewModel.updateProfileImageResponse.observe(this) { response ->
            if (response != null && response.success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Go back to the previous screen
            } else {
                Toast.makeText(
                    this,
                    response?.message ?: "Failed to update profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setOnClick() {
        binding.imgProfile.setOnClickListener {
            imagePickerResult.launch("image/*") // Open image picker
        }
        binding.btnSave.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmail.text.toString()
            val description = binding.edtDescription.text.toString()
            val token = SharedPrefManager.getToken(this) ?: ""
            if (GlobelMethods().isValidEmail(email)) {
                userViewModel.updateUserDetails(
                    token,
                    UpdateUserRequest(username, email, description)
                )
            } else {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            }
            if (selectedImageUri != null) {
                val file = getFileFromUri(this, selectedImageUri!!)
                if (file != null) {
                    userViewModel.updateProfileImage(token, file)
                } else {
                    Toast.makeText(this, "Failed to process selected image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun initView() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        SharedPrefManager.getUserId(this)
            ?.let { SharedPrefManager.getToken(this)?.let { it1 -> fetchUserData(it, it1) } }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fetchUserData(userId: String, token: String) {
        observeUserData()
        userViewModel.fetchUserData(userId, token)
    }

    private fun observeUserData() {
        userViewModel.userData.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userDetails = userResponse.data.user
                    updateUserUI(userDetails)
                }
            }
        }

        userViewModel.updateUserResponse.observe(this) { responce ->
            if (responce.code() == 200) {
                Toast.makeText(this@EditProfileActivity, responce.message(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun updateUserUI(userDetails: UserDetails) {
        binding.edtUsername.setText(userDetails.username ?: "")
        binding.edtEmail.setText(userDetails.email ?: "")
        binding.edtDescription.setText(userDetails.description ?: "")

        if (!userDetails.profileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(userDetails.profileImageUrl)
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .into(binding.imgProfile)
        } else {
            binding.imgProfile.setImageResource(R.drawable.ic_account)
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onResume() {
        super.onResume()
        checkImagePermission()
    }

    private fun checkImagePermission() {
        if (!isReadStoragePermissionGranted()) {
            requestReadStoragePermission()
        }
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestReadStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(
                this,
                "Permission is required to access images.",
                Toast.LENGTH_LONG
            ).show()
        }

        requestPermissionLauncher.launch(permission)
    }

}
