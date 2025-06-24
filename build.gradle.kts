// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false // Declare KAPT plugin at project level as well
    alias(libs.plugins.hilt.android) apply false // Declare Hilt plugin at project level
    alias(libs.plugins.kotlin.parcelize) apply false // Thêm dòng này
}