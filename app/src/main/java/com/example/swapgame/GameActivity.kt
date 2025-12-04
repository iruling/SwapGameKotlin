package com.example.swapgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

class GameActivity : AppCompatActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var tvPageIndicator: TextView
    private lateinit var gameContainer: ConstraintLayout
    private lateinit var gestureDetector: GestureDetector
    
    private var targetDirection: String = "Gauche"
    private var currentPage: String = "Gauche"
    private var timeRemaining: Int = 10
    private var isFrozen: Boolean = false
    private var isGameEnded: Boolean = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var lastSwapTime: Long = 0
    private var inactivityCheckRunnable: Runnable? = null
    
    companion object {
        private const val INACTIVITY_CHECK_INTERVAL_MS = 500L
        private const val INACTIVITY_FREEZE_THRESHOLD_MS = 2000L
        private const val SWIPE_THRESHOLD_PX = 100
        private const val SWIPE_VELOCITY_THRESHOLD_PX_PER_SEC = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        tvCountdown = findViewById(R.id.tv_countdown)
        tvPageIndicator = findViewById(R.id.tv_page_indicator)
        gameContainer = findViewById(R.id.game_container)
        
        // Initialize gesture detector for swipe detection
        gestureDetector = GestureDetector(this, SwipeGestureListener())
        
        showInstructionPopup()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun showInstructionPopup() {
        val directions = listOf("Gauche", "Droite")
        targetDirection = directions.random()
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instruction")
        builder.setMessage("Allez à : $targetDirection")
        builder.setCancelable(false)
        
        val dialog = builder.create()
        dialog.show()

        // Close popup after 3 seconds and start game
        handler.postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            startGame()
        }, 3000)
    }
    
    private fun startGame() {
        lastSwapTime = System.currentTimeMillis()
        updatePageIndicator()
        startCountdown()
        startInactivityCheck()
    }
    
    private fun startCountdown() {
        val countdownRunnable = object : Runnable {
            override fun run() {
                if (isGameEnded) return
                
                tvCountdown.text = timeRemaining.toString()
                
                // Hide countdown when reaching 5 seconds
                if (timeRemaining <= 5) {
                    tvCountdown.visibility = View.INVISIBLE
                }
                
                if (timeRemaining > 0) {
                    timeRemaining--
                    handler.postDelayed(this, 1000)
                } else {
                    endGame()
                }
            }
        }
        handler.post(countdownRunnable)
    }
    
    private fun startInactivityCheck() {
        inactivityCheckRunnable = object : Runnable {
            override fun run() {
                if (isGameEnded) return
                
                val timeSinceLastSwap = System.currentTimeMillis() - lastSwapTime
                
                // If no swap for 2 seconds, freeze the screen
                if (timeSinceLastSwap >= INACTIVITY_FREEZE_THRESHOLD_MS && !isFrozen) {
                    freezeScreen()
                }
                
                handler.postDelayed(this, INACTIVITY_CHECK_INTERVAL_MS)
            }
        }
        inactivityCheckRunnable?.let { handler.post(it) }
    }
    
    private fun freezeScreen() {
        isFrozen = true
        gameContainer.setBackgroundColor(Color.BLACK)
        tvCountdown.visibility = View.INVISIBLE
        tvPageIndicator.visibility = View.INVISIBLE
        
        // Unfreeze after 1 second and auto-swap
        handler.postDelayed({
            if (!isGameEnded) {
                unfreezeScreen()
                performAutoSwap()
            }
        }, 1000)
    }
    
    private fun unfreezeScreen() {
        isFrozen = false
        gameContainer.setBackgroundColor(Color.WHITE)
        
        // Only show countdown if it should still be visible (timeRemaining > 5)
        if (timeRemaining > 5) {
            tvCountdown.visibility = View.VISIBLE
        }
        tvPageIndicator.visibility = View.VISIBLE
    }
    
    private fun performAutoSwap() {
        currentPage = if (currentPage == "Gauche") "Droite" else "Gauche"
        updatePageIndicator()
        lastSwapTime = System.currentTimeMillis()
    }
    
    private fun swapPage() {
        if (isFrozen || isGameEnded) return
        
        currentPage = if (currentPage == "Gauche") "Droite" else "Gauche"
        updatePageIndicator()
        lastSwapTime = System.currentTimeMillis()
    }
    
    private fun updatePageIndicator() {
        tvPageIndicator.text = "Page: $currentPage"
    }
    
    private fun endGame() {
        isGameEnded = true
        handler.removeCallbacksAndMessages(null)
        
        // Check if screen is frozen (black) or if on correct page
        val isWin = !isFrozen && currentPage == targetDirection
        
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("IS_WIN", isWin)
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
    
    // Inner class for swipe gesture detection
    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD_PX && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD_PX_PER_SEC) {
                    swapPage()
                    return true
                }
            }
            return false
        }

    }
}
