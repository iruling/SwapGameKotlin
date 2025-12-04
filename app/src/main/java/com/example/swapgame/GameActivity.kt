package com.example.swapgame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        showInstructionPopup()
    }

    private fun showInstructionPopup() {
        val directions = listOf("Gauche", "Droite")
        val targetDirection = directions.random()
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instruction")
        builder.setMessage("Allez à : $targetDirection")
        builder.setCancelable(false)
        
        val dialog = builder.create()
        dialog.show()

        // Fermer la popup après 3 secondes (3000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }
}