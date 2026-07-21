package org.example.test.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.example.test.launch.LaunchStateHolder

// The single screen MainActivity sets as content. It owns the transition
// from the skeleton loading state into the primary interface; no other
// screen needs to know the app has a startup phase at all.
@Composable
fun LaunchScreen(stateHolder: LaunchStateHolder = remember { LaunchStateHolder() }) {
    val launchState by stateHolder.launchState

    LaunchedEffect(stateHolder) {
        stateHolder.start()
    }

    Crossfade(
        targetState = launchState,
        modifier = Modifier.fillMaxSize(),
        animationSpec = tween(durationMillis = 400),
        label = "launch_transition",
    ) { currentLaunchState ->
        currentLaunchState.Content()
    }
}
