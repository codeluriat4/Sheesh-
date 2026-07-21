/*
 * Root build file. Declares plugins for the whole project without applying
 * them to the root; the app module applies what it needs.
 */

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}
