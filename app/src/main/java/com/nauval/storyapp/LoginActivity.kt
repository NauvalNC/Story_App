package com.nauval.storyapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.nauval.storyapp.databinding.ActivityLoginBinding
import com.nauval.storyapp.factory.UserViewModelFactory
import com.nauval.storyapp.helper.EspressoIdlingResource
import com.nauval.storyapp.helper.IGeneralSetup
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.StoryApiSession
import com.nauval.storyapp.repository.UserRepository
import com.nauval.storyapp.viewmodel.UserLoginViewModel

class LoginActivity : AppCompatActivity(), IGeneralSetup {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: UserLoginViewModel

    private val launcherRegister = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == REGISTER_RESULT && result.data != null) {
            binding.apply {
                emailField.setText(result.data?.getStringExtra(EMAIL_EXTRA), TextView.BufferType.EDITABLE)
                passwordField.setText(result.data?.getStringExtra(PASS_EXTRA), TextView.BufferType.EDITABLE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.app_name)

        loginViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(StoryApiConfig.getUserApiService()))
        )[UserLoginViewModel::class.java]

        loginViewModel.apply {
            response.observe(this@LoginActivity) {
                StoryApiSession(this@LoginActivity).setToken(it.loginResultResponse.token)
                Toast.makeText(
                    this@LoginActivity,
                    resources.getString(R.string.login_success),
                    Toast.LENGTH_SHORT
                ).show()
                openHome()
            }

            isError.observe(this@LoginActivity){
                EspressoIdlingResource.decrement()
                if (it) {
                    enableControl(true)
                    Toast.makeText(
                        this@LoginActivity,
                        resources.getString(R.string.login_failed),
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

    override fun setup() {
        binding.apply {
            registerBtn.setOnClickListener {
                launcherRegister.launch(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
            loginBtn.setOnClickListener { loginAccount() }
        }

        startAnimation()
    }

    override fun isFieldVerified(): Boolean {
        return (binding.emailField.isValid(withToast = true) && binding.passwordField.isValid(withToast = true))
    }

    override fun enableControl(isEnabled: Boolean) {
        binding.apply {
            loadingBar.visibility = if (isEnabled) View.GONE else View.VISIBLE
            loginBtn.isEnabled = isEnabled
            registerBtn.isEnabled = isEnabled
        }
    }

    override fun startAnimation() {
        binding.apply {
            val dur = IGeneralSetup.ANIM_DURATION

            val mIconTitle = ObjectAnimator.ofFloat(iconTitle, View.ALPHA, 1f).setDuration(dur)
            val mEmailField = ObjectAnimator.ofFloat(emailField, View.ALPHA, 1f).setDuration(dur)
            val mPassField = ObjectAnimator.ofFloat(passwordField, View.ALPHA, 1f).setDuration(dur)
            val mLoginBtn = ObjectAnimator.ofFloat(loginBtn, View.ALPHA, 1f).setDuration(dur)
            val mRegisterTxt = ObjectAnimator.ofFloat(registerText, View.ALPHA, 1f).setDuration(dur)
            val mRegisterBtn = ObjectAnimator.ofFloat(registerBtn, View.ALPHA, 1f).setDuration(dur)

            AnimatorSet().apply {
                playSequentially(mIconTitle, mEmailField, mPassField, mLoginBtn, mRegisterTxt, mRegisterBtn)
                start()
            }
        }
    }

    private fun loginAccount() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (isFieldVerified()) {
            enableControl(false)

            val mEmail = binding.emailField.text.toString()
            val mPassword = binding.passwordField.text.toString()

            EspressoIdlingResource.increment()
            loginViewModel.loginUser(mEmail, mPassword)
        }
    }

    private fun openHome() {
        val intent = Intent(this@LoginActivity, MainLandingPageActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EMAIL_EXTRA = "EMAIL"
        const val PASS_EXTRA = "PASS"
        const val REGISTER_RESULT = 175
    }
}