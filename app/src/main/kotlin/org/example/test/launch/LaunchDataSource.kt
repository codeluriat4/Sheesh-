package org.example.test.launch

// Owns the app's startup preparation work. The launch flow depends on this
// interface rather than a concrete implementation, so swapping the fixed
// delay below for real startup work (cache warmup, session check, remote
// config fetch, etc.) never requires touching the launch flow itself.
interface LaunchDataSource {
    suspend fun prepare()
}
