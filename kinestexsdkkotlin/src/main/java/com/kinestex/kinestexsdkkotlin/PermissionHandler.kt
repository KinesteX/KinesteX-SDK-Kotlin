package com.kinestex.kinestexsdkkotlin

/**
 * Interface for handling camera permission requests
 *
 * Implement this interface in your Activity or Fragment to handle
 * camera permission requests from the KinesteX WebView.
 *
 * Example implementation:
 * ```kotlin
 * class MainActivity : AppCompatActivity(), PermissionHandler {
 *     private val requestPermissionLauncher = registerForActivityResult(
 *         ActivityResultContracts.RequestPermission()
 *     ) { isGranted ->
 *         kinestexWebView?.handlePermissionResult(isGranted)
 *     }
 *
 *     override fun requestCameraPermission() {
 *         requestPermissionLauncher.launch(Manifest.permission.CAMERA)
 *     }
 * }
 * ```
 */
interface PermissionHandler {
    /**
     * Called when the WebView needs camera permission
     *
     * Implement this method to show the system permission dialog.
     * Pass the result back to the WebView using [GenericWebView.handlePermissionResult]
     */
    fun requestCameraPermission()
}