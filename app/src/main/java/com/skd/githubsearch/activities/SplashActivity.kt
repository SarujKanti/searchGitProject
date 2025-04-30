package com.skd.githubsearch.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.skd.githubsearch.R
import com.skd.githubsearch.databinding.ActivityMainDashboardBinding
import com.skd.githubsearch.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_splash)

        val txtAppName: TextView = findViewById(R.id.txt_appName)
        txtAppName.text = getString(R.string.app_title)

        val handler = android.os.Handler()

        val runnable = Runnable {
            val intent = Intent(this@SplashActivity, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        handler.postDelayed(runnable, 2000)
    }
}