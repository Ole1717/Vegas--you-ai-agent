package com.agent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.TextView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = "Agent\n\nПервый запуск"
            textSize = 24f
            setPadding(48, 48, 48, 48)
        }

        setContentView(text)
    }
}
