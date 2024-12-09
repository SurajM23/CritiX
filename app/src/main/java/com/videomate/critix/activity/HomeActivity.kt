package com.videomate.critix.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.videomate.critix.fragments.UploadFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.videomate.critix.R
import com.videomate.critix.fragments.ExploreFragment
import com.videomate.critix.fragments.HomeFragment
import com.videomate.critix.fragments.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val exploreFragment = ExploreFragment()
    private val uploadFragment = UploadFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Add the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HomeFragment")
                .commit()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment, "HomeFragment")
                R.id.nav_explore -> showFragment(exploreFragment, "ExploreFragment")
                R.id.nav_upload -> showFragment(uploadFragment, "UploadFragment")
                R.id.nav_profile -> showFragment(profileFragment, "ProfileFragment")
            }
            true
        }
    }

    private fun showFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // Hide all other fragments
        supportFragmentManager.fragments.forEach { existingFragment ->
            transaction.hide(existingFragment)
        }

        // Check if the fragment is already added
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            transaction.add(R.id.fragment_container, fragment, tag)
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
    }
}
