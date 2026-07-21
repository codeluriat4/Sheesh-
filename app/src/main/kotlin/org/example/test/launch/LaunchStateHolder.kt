package org.example.test.launch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import org.example.test.ui.state.LaunchUiState

// Owns the current launch state and the single behavior that advances it.
// Nothing outside this class ever assigns the state directly, keeping the
// data and the rule that governs its transition in one place.
class LaunchStateHolder(
    private val dataSource: LaunchDataSource = SimulatedLaunchDataSource(),
) {
    private val mutableLaunchState = mutableStateOf<LaunchUiState>(LaunchUiState.Skeleton)
    val launchState: State<LaunchUiState> get() = mutableLaunchState

    suspend fun start() {
        dataSource.prepare()
        mutableLaunchState.value = LaunchUiState.Ready
    }
}
