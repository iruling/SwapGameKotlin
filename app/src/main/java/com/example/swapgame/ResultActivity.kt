package com.example.swapgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val isWin = intent.getBooleanExtra("IS_WIN", false)
        val tvResult = findViewById<TextView>(R.id.tv_result)
        
        tvResult.text = if (isWin) "Gagné !" else "Perdu !"

        val btnBackToMenu = findViewById<Button>(R.id.btn_back_to_menu)
        btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
