package com.nauval.storyapp.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.nauval.storyapp.R
import com.nauval.storyapp.helper.StoryApiConfig

class EditTextWithErrorPrompter: AppCompatEditText {

    private val errorImage = ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_outline_24) as Drawable
    private val originalColor = ContextCompat.getColor(context, R.color.accent_1)
    private val errorColor = ContextCompat.getColor(context, R.color.warning)

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        errorImage.setTint(errorColor)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isValid(s.toString(), false)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    @SuppressLint("RestrictedApi")
    private fun showError() {
        setEditTextDrawable(end = errorImage)
        supportBackgroundTintList = ColorStateList.valueOf(errorColor)
    }

    @SuppressLint("RestrictedApi")
    private fun hideError() {
        setEditTextDrawable()
        supportBackgroundTintList = ColorStateList.valueOf(originalColor)
    }

    fun isValid(data: String = text.toString(), withToast: Boolean): Boolean {
        when (inputType - 1) {
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                if (data.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(data).matches()) {
                    if (data.isEmpty()) {
                        if (withToast) Toast.makeText(
                            context,
                            resources.getString(R.string.field_cannot_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (withToast) Toast.makeText(
                            context,
                            resources.getString(R.string.invalid_email),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showError()

                    return false
                }
                else hideError()
            }
            InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                if (data.isEmpty() || data.length < StoryApiConfig.MIN_PASSWORD) {
                    showError()

                    if (withToast) Toast.makeText(
                        context,
                        resources.getString(R.string.invalid_password, StoryApiConfig.MIN_PASSWORD),
                        Toast.LENGTH_SHORT
                    ).show()

                    return false
                }
                else hideError()
            }
            else -> {
                if (data.isEmpty()) {
                    showError()

                    if (withToast) Toast.makeText(
                        context,
                        resources.getString(R.string.field_cannot_empty),
                        Toast.LENGTH_SHORT
                    ).show()

                    return false
                }
                else hideError()
            }
        }

        return true
    }

    private fun setEditTextDrawable(start: Drawable? = null, top: Drawable? = null,
        end: Drawable? = null, bottom: Drawable? = null
    ) { setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }
}