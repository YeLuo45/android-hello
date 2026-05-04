package com.hello.android

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hello.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.btnGreet.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "请输入名字", Toast.LENGTH_SHORT).show()
                binding.cardGreeting.visibility = View.GONE
            } else {
                binding.tvGreeting.text = "你好, $name!"
                binding.cardGreeting.visibility = View.VISIBLE
            }
        }
    }
}
