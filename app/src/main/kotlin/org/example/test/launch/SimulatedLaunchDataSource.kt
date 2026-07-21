package org.example.test.launch

import kotlinx.coroutines.delay

// Default startup work: a fixed minimum skeleton duration so the loading
// state is perceptible even before the app has real startup work to wait on.
class SimulatedLaunchDataSource(
    private val minimumDurationMillis: Long = 1200L,
) : LaunchDataSource {
    override suspend fun prepare() {
        delay(minimumDurationMillis)
    }
}
