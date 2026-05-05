package com.hello.android

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hello.android.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.btnGreet.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                Timber.d("Name is empty, hiding greeting")
                Toast.makeText(this, "请输入名字", Toast.LENGTH_SHORT).show()
                binding.cardGreeting.visibility = View.GONE
            } else {
                Timber.d("Greeting user: %s", name)
                binding.tvGreeting.text = "你好, $name!"
                binding.cardGreeting.visibility = View.VISIBLE
            }
        }
    }
}
