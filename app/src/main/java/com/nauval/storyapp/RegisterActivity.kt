package com.nauval.storyapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.nauval.storyapp.databinding.ActivityRegisterBinding
import com.nauval.storyapp.factory.UserViewModelFactory
import com.nauval.storyapp.helper.EspressoIdlingResource
import com.nauval.storyapp.helper.IGeneralSetup
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.repository.UserRepository
import com.nauval.storyapp.viewmodel.UserRegisterViewModel

class RegisterActivity : AppCompatActivity(), IGeneralSetup {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: UserRegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.create_an_account)

        registerViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(StoryApiConfig.getUserApiService()))
        )[UserRegisterViewModel::class.java]

        registerViewModel.apply {
            response.observe(this@RegisterActivity) {
                Toast.makeText(
                    this@RegisterActivity,
                    resources.getString(R.string.register_success),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccessRegister()
            }

            isError.observe(this@RegisterActivity){
                EspressoIdlingResource.decrement()
                if (it) {
                    enableControl(true)
                    Toast.makeText(
                        this@RegisterActivity,
                        resources.getString(R.string.register_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        setup()
    }

    override fun onResume() {
        super.onResume()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setup() {
        binding.registerBtn.setOnClickListener { registerAccount() }
        startAnimation()
    }

    override fun isFieldVerified(): Boolean {
        return (binding.nameField.isValid(withToast = true)
                && binding.emailField.isValid(withToast = true)
                && binding.passwordField.isValid(withToast = true))
    }

    override fun enableControl(isEnabled: Boolean) {
        binding.apply {
            loadingBar.visibility = if (isEnabled) View.GONE else View.VISIBLE
            registerBtn.isEnabled = isEnabled
        }
    }

    override fun startAnimation() {
        binding.apply {
            val dur = IGeneralSetup.ANIM_DURATION

            val mIconTitle = ObjectAnimator.ofFloat(iconTitle, View.ALPHA, 1f).setDuration(dur)
            val mNameField = ObjectAnimator.ofFloat(nameField, View.ALPHA, 1f).setDuration(dur)
            val mEmailField = ObjectAnimator.ofFloat(emailField, View.ALPHA, 1f).setDuration(dur)
            val mPassField = ObjectAnimator.ofFloat(passwordField, View.ALPHA, 1f).setDuration(dur)
            val mRegisterBtn = ObjectAnimator.ofFloat(registerBtn, View.ALPHA, 1f).setDuration(dur)

            AnimatorSet().apply {
                playSequentially(mIconTitle, mNameField, mEmailField, mPassField, mRegisterBtn)
                start()
            }
        }
    }

    private fun registerAccount() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (isFieldVerified()) {
            enableControl(false)

            val mName = binding.nameField.text.toString()
            val mEmail = binding.emailField.text.toString()
            val mPassword = binding.passwordField.text.toString()

            EspressoIdlingResource.increment()
            registerViewModel.registerUser(mName, mEmail, mPassword)
        }
    }

    private fun onSuccessRegister() {
        setResult(LoginActivity.REGISTER_RESULT, Intent().apply {
            putExtra(LoginActivity.EMAIL_EXTRA, binding.emailField.text.toString())
            putExtra(LoginActivity.PASS_EXTRA, binding.passwordField.text.toString())
        })
        finish()
    }
}