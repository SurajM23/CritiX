package com.videomate.critix.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.videomate.critix.R
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentUploadBinding
import com.videomate.critix.model.ReviewRequest
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class UploadFragment : Fragment(R.layout.fragment_upload) {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel
    private val globalTagsList = mutableListOf<String>()

    private val availableTags = mutableListOf(
        "Good Direction",
        "Great Acting",
        "Amazing Cinematography",
        "Thrilling Plot",
        "Interesting Characters",
        "Great Soundtrack",
        "Fantastic Visual Effects",
        "Action-Packed",
        "Emotional Story",
        "Plot Twists",
        "Best Sequel",
        "Must-Watch",
        "Horror",
        "Romantic",
        "Comedy",
        "Historical Drama",
        "Heartwarming",
        "Inspiring",
        "Best Cinematography",
        "Oscar-worthy Performances",
        "Bad Direction",
        "Poor Acting",
        "Boring Plot",
        "Bad Cinematography"
    ).sorted()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUploadBinding.bind(view)
        init()
        setObserver()
        binding.tagsEditText.setOnClickListener {
            showTagsDialog()
        }

        binding.uploadReviewButton.setOnClickListener {
            if (binding.movieTitleEditText.text.isNotEmpty() && binding.reviewTextEditText.text.isNotEmpty()) {
                SharedPrefManager.getToken(requireContext())?.let { it1 ->
                    val cleanedReviewText = binding.reviewTextEditText.text.toString().replace("\n", " ").trim()
                    userViewModel.createReview(
                        it1,
                        ReviewRequest(
                            binding.movieTitleEditText.text.toString().replace("\n", " ").trim(),
                            binding.reviewTextEditText.text.toString().trim(),
                            binding.ratingSeekBar.rating.toInt(), // Use actual rating value
                            globalTagsList
                        )
                    )
                }
            } else {
                Snackbar.make(view, "Please fill in all fields.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.ratingSeekBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(requireContext(), "Selected Rating: $rating", Toast.LENGTH_SHORT).show()
            "$rating stars".also { binding.ratingTextView.text = it }
        }

    }

    private fun setObserver() {
        userViewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            Log.e("uploadingreview","respnse ${response.raw()}")
            if (response.code() == 200) {
                Constants.ReviewUploaded1 = true
                binding.movieTitleEditText.setText("")
                binding.tagsEditText.text = ""
                binding.ratingTextView.text = ""
                binding.ratingSeekBar.rating = 0.0F
                binding.reviewTextEditText.setText("")
            }else{
                Toast.makeText(requireContext(), "Failed ${response.body()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }


    private fun showTagsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Tags")

        val tagsArray = availableTags.toTypedArray()
        val selectedArray =
            BooleanArray(availableTags.size) { globalTagsList.contains(availableTags[it]) }

        builder.setMultiChoiceItems(
            tagsArray,
            selectedArray
        ) { _, which, isChecked ->
            val selectedTag = tagsArray[which]
            if (isChecked) {
                if (!globalTagsList.contains(selectedTag)) {
                    globalTagsList.add(selectedTag) // Add tag to global list
                }
            } else {
                globalTagsList.remove(selectedTag) // Remove tag from global list
            }
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            // Update the tags TextView to display the globalTagsList
            updateTagsTextView(globalTagsList)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }


    private fun updateTagsTextView(tags: List<String>) {
        binding.tagsEditText.text = tags.joinToString(", ")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding reference to avoid memory leaks
    }
}
