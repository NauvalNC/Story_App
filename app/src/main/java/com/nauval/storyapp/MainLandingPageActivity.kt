package com.nauval.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.nauval.storyapp.databinding.ActivityMainLandingPageBinding
import com.nauval.storyapp.helper.IGeneralSetup

class MainLandingPageActivity : AppCompatActivity(), IGeneralSetup {
    private lateinit var binding: ActivityMainLandingPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    override fun setup() {
        val navCtrl = findNavController(R.id.bottom_nav_fragment)
        setupActionBarWithNavController(navCtrl, AppBarConfiguration.Builder(setOf(R.id.nav_story, R.id.nav_map)).build())
        binding.bottomNav.setupWithNavController(navCtrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_option_menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting_menu -> {
                startActivity(Intent(this@MainLandingPageActivity, SettingActivity::class.java))
                true
            }
            else -> true
        }
    }

    companion object {
        const val NEW_STORY_ADDED_RESULT = 150
    }
}