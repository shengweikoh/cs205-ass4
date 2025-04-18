package com.example.cs205_ass4.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Utility class to simulate IO operations with visual feedback Shows a popup dialog and greys out
 * the screen during simulated operations
 */
class IOSimulator(private val context: Context, private val rootView: ViewGroup) {
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: FrameLayout? = null
    private var dialog: AlertDialog? = null

    /**
     * Simulates an IO operation with visual feedback
     *
     * @param operationName The name of the operation to display in the popup
     * @param durationMs The duration of the simulated operation in milliseconds
     * @param onStart Optional callback that runs when simulation starts
     * @param onComplete Optional callback that runs when simulation completes
     */
    fun simulateIO(
            operationName: String,
            durationMs: Long = 1000,
            onStart: (() -> Unit)? = null,
            onComplete: (() -> Unit)? = null
    ) {
        // Create and show the grey overlay
        showOverlay()

        // Show the dialog
        showDialog(operationName)

        // Call the onStart callback if provided
        onStart?.invoke()

        // Schedule removal of overlay and dialog after the duration
        handler.postDelayed(
                {
                    // Remove the overlay
                    removeOverlay()

                    // Dismiss the dialog
                    dialog?.dismiss()
                    dialog = null

                    // Call the onComplete callback if provided
                    onComplete?.invoke()
                },
                durationMs
        )
    }

    /** Creates and shows a semi-transparent grey overlay over the screen */
    private fun showOverlay() {
        // Create a new overlay if it doesn't exist
        if (overlay == null) {
            overlay =
                    FrameLayout(context).apply {
                        layoutParams =
                                ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                )
                        setBackgroundColor(Color.parseColor("#99000000")) // Semi-transparent grey
                    }
        }

        // Add the overlay to the root view
        overlay?.let {
            if (it.parent == null) {
                rootView.addView(it)

                // Vibrate the device when overlay appears
                vibrate()
            }
        }
    }

    /** Makes the device vibrate */
    private fun vibrate() {
        try {
            // Get vibrator service based on Android version
            val vibrator =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager =
                                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as
                                        VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

            // Check if device can vibrate
            if (vibrator.hasVibrator()) {
                // Create vibration effect based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For Android 8.0 (API 26) and above, use VibrationEffect
                    val vibrationEffect =
                            VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    // For older versions
                    @Suppress("DEPRECATION") vibrator.vibrate(200)
                }
            }
        } catch (e: Exception) {
            // Handle any potential exceptions silently - vibration is non-critical
        }
    }

    /** Removes the grey overlay from the screen */
    private fun removeOverlay() {
        overlay?.let { rootView.removeView(it) }
        overlay = null
    }

    /** Shows a dialog with the operation message */
    private fun showDialog(operationName: String) {
        // Create a text view for the message
        val messageView =
                TextView(context).apply {
                    text = operationName
                    textSize = 18f
                    setTextColor(Color.BLACK)
                    setPadding(32, 32, 32, 32)
                }

        // Create and show the dialog
        dialog =
                AlertDialog.Builder(context)
                        .setTitle("I/O Operation in Progress")
                        .setView(messageView)
                        .setCancelable(false)
                        .create()

        dialog?.show()
    }
}
