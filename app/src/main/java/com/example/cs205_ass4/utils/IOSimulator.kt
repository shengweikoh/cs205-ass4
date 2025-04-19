package com.example.cs205_ass4.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import java.util.Random

/**
 * Utility class to simulate IO operations with visual feedback Shows a popup dialog and greys out
 * the screen during simulated operations
 */
class IOSimulator(private val context: Context, private val rootView: ViewGroup) {
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: AnimatedOverlayView? = null
    private var dialog: AlertDialog? = null
    private var dialogView: View? = null
    private var pulsatingAnimator: ValueAnimator? = null

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
        // Create and show the animated overlay
        showOverlay()

        // Show the dialog with pulsating animation
        showDialog(operationName)

        // Call the onStart callback if provided
        onStart?.invoke()

        // Schedule removal of overlay and dialog after the duration
        handler.postDelayed(
                {
                    // Remove the overlay with fade-out animation
                    removeOverlay()

                    // Stop pulsating animation
                    pulsatingAnimator?.cancel()
                    pulsatingAnimator = null

                    // Dismiss the dialog
                    dialog?.dismiss()
                    dialog = null
                    dialogView = null

                    // Call the onComplete callback if provided
                    onComplete?.invoke()
                },
                durationMs
        )
    }

    /** Creates and shows an animated overlay with advanced 2D graphics */
    private fun showOverlay() {
        // Create a new overlay if it doesn't exist
        if (overlay == null) {
            overlay = AnimatedOverlayView(context)
        }

        // Add the overlay to the root view with fade-in animation
        overlay?.let {
            if (it.parent == null) {
                it.alpha = 0f
                rootView.addView(it)

                // Fade in animation
                it.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()

                // Start the particle animation
                it.startAnimation()

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

    /** Removes the overlay with animation */
    private fun removeOverlay() {
        overlay?.let {
            // Stop particle animation
            it.stopAnimation()

            // Fade out animation
            it.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setListener(
                            object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    rootView.removeView(it)
                                    overlay = null
                                }
                            }
                    )
                    .start()
        }
    }

    /** Shows a dialog with the operation message and pulsating animation */
    private fun showDialog(operationName: String) {
        // Create a text view for the message
        val messageView =
                TextView(context).apply {
                    text = operationName
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    setPadding(32, 32, 32, 32)
                }

        // Create and show the dialog with custom style
        dialog =
                AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setTitle("I/O Operation in Progress")
                        .setView(messageView)
                        .setCancelable(false)
                        .create()

        dialog?.show()

        // Get the dialog's window decoration view
        dialog?.window?.decorView?.let { decorView ->
            dialogView = decorView

            // Apply pulsating animation to dialog
            applyPulsatingAnimation(decorView)
        }
    }

    /** Applies a pulsating scale animation to the dialog */
    private fun applyPulsatingAnimation(view: View) {
        pulsatingAnimator =
                ValueAnimator.ofFloat(1.0f, 1.05f).apply {
                    duration = 800
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener { animation ->
                        val scale = animation.animatedValue as Float
                        view.scaleX = scale
                        view.scaleY = scale
                    }

                    start()
                }
    }

    /**
     * Custom view that implements an animated gradient background with floating particles to
     * simulate data flow during I/O operations
     */
    private inner class AnimatedOverlayView(context: Context) : View(context) {
        private val particles = ArrayList<Particle>()
        private val random = Random()
        private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var animating = false
        private val updateHandler = Handler(Looper.getMainLooper())
        private val gradientColors =
                intArrayOf(
                        Color.parseColor("#44000080"), // Semi-transparent dark blue
                        Color.parseColor("#77003366"), // Medium transparent blue
                        Color.parseColor("#99000080") // More opaque dark blue
                )
        private var gradientYOffset = 0f
        private val backgroundPaint =
                Paint().apply {
                    color = Color.parseColor("#BB000000") // Semi-transparent black
                }

        // Runnable for animation updates
        private val animationRunnable =
                object : Runnable {
                    override fun run() {
                        if (animating) {
                            updateParticles()
                            updateGradient()
                            invalidate()
                            updateHandler.postDelayed(this, 16) // ~60fps
                        }
                    }
                }

        init {
            // Enable hardware acceleration for better performance
            setLayerType(LAYER_TYPE_HARDWARE, null)

            // Configure the particle paint
            particlePaint.color = Color.WHITE
            particlePaint.alpha = 180
            particlePaint.style = Paint.Style.FILL

            // Set up blending mode for the gradient
            gradientPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }

        fun startAnimation() {
            // Create initial particles
            createParticles(25)
            
            // Start animation loop
            animating = true
            updateHandler.post(animationRunnable)
        }

        fun stopAnimation() {
            animating = false
            updateHandler.removeCallbacks(animationRunnable)
        }

        private fun createParticles(count: Int) {
            particles.clear()
            for (i in 0 until count) {
                particles.add(Particle(
                    x = random.nextFloat() * width,
                    y = random.nextFloat() * height,
                    radius = 12f + random.nextFloat() * 18f,
                    speedX = -2f + random.nextFloat() * 4f,
                    speedY = -2f + random.nextFloat() * 4f,
                    alpha = 180 + random.nextInt(75),
                    color = getRandomDataColor()
                ))
            }
        }

        private fun getRandomDataColor(): Int {
            val colors =
                    arrayOf(
                            Color.parseColor("#4CAF50"), // Green
                            Color.parseColor("#2196F3"), // Blue
                            Color.parseColor("#FFC107"), // Yellow
                            Color.parseColor("#F44336"), // Red
                            Color.parseColor("#9C27B0"), // Purple
                            Color.parseColor("#9C27B0") // Purple
                    )
            return colors[random.nextInt(colors.size)]
        }

        private fun updateParticles() {
            for (particle in particles) {
                // Update position
                particle.x += particle.speedX
                particle.y += particle.speedY

                // Wrap around screen bounds
                if (particle.x < 0) particle.x = width.toFloat()
                if (particle.x > width) particle.x = 0f
                if (particle.y < 0) particle.y = height.toFloat()
                if (particle.y > height) particle.y = 0f

                // Randomly change direction occasionally
                if (random.nextFloat() < 0.01f) {
                    particle.speedX = -2f + random.nextFloat() * 4f
                    particle.speedY = -2f + random.nextFloat() * 4f
                }
            }
        }

        private fun updateGradient() {
            // Move the gradient for flowing effect
            gradientYOffset += 2f
            if (gradientYOffset > height) {
                gradientYOffset = 0f
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            // Create particles based on new size
            if (particles.isEmpty()) {
                createParticles(25) 
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            // Draw semi-transparent background
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            
            // Draw animated gradient
            val gradient = LinearGradient(
                0f, gradientYOffset - height, width.toFloat(), gradientYOffset,
                gradientColors, null, Shader.TileMode.MIRROR
            )
            gradientPaint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
            
            // Draw particles
            for (particle in particles) {
                // Draw glow effect (larger circle with lower opacity)
                particlePaint.color = particle.color
                particlePaint.alpha = particle.alpha / 3
                canvas.drawCircle(particle.x, particle.y, particle.radius * 1.8f, particlePaint)
                
                // Draw the main particle
                particlePaint.color = particle.color
                particlePaint.alpha = particle.alpha
                canvas.drawCircle(particle.x, particle.y, particle.radius, particlePaint)
                
                // Draw "data trail" lines between particles
                if (random.nextFloat() < 0.3f) { // Increased chance for connections
                    val nearestParticle = findNearestParticle(particle, 400f) // Increased connection range
                    if (nearestParticle != null) {
                        particlePaint.alpha = 100
                        particlePaint.strokeWidth = 4f
                        canvas.drawLine(particle.x, particle.y, nearestParticle.x, nearestParticle.y, particlePaint)
                    }
                }
            }
        }

        private fun findNearestParticle(source: Particle, maxDistance: Float): Particle? {
            var nearest: Particle? = null
            var minDistance = maxDistance

            for (particle in particles) {
                if (particle != source) {
                    val distance = distance(source.x, source.y, particle.x, particle.y)
                    if (distance < minDistance) {
                        minDistance = distance
                        nearest = particle
                    }
                }
            }

            return nearest
        }

        private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val dx = x2 - x1
            val dy = y2 - y1
            return kotlin.math.sqrt(dx * dx + dy * dy)
        }
    }

    /** Data class representing a particle in the animation */
    private data class Particle(
            var x: Float,
            var y: Float,
            val radius: Float,
            var speedX: Float,
            var speedY: Float,
            val alpha: Int,
            val color: Int
    )
}
