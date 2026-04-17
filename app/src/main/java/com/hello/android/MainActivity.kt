package com.hello.android

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Hello Android!\n\n环境验证成功"
            textSize = 24f
            setPadding(50, 50, 50, 50)
        }

        setContentView(textView)
    }
}
