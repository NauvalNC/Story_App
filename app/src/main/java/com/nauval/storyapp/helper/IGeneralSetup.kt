package com.nauval.storyapp.helper

interface IGeneralSetup {
    fun setup()
    fun isFieldVerified(): Boolean = false
    fun enableControl(isEnabled: Boolean) {}
    fun startAnimation() {}

    companion object {
        const val ANIM_DURATION = 400L
    }
}