package com.example.homelandscape

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.homelandscape.ar.ArEngineSelector
import com.example.homelandscape.ar.ArSessionFacade
import com.example.homelandscape.databinding.ActivityMainBinding
import com.example.homelandscape.ui.CaptureActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var arSessionFacade: ArSessionFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arSessionFacade = ArSessionFacade(this)
        val backendName = ArEngineSelector.displayName(arSessionFacade.backend())
        binding.textArStatus.text = getString(R.string.main_ar_status, backendName)

        binding.buttonOpenCapture.setOnClickListener {
            startActivity(Intent(this, CaptureActivity::class.java))
        }
    }

    override fun onDestroy() {
        arSessionFacade.release()
        super.onDestroy()
    }
}
