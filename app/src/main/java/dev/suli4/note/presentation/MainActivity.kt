package dev.suli4.note.presentation

import android.os.Bundle
import android.os.FileUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.databinding.ActivityMainBinding
import dev.suli4.note.utils.saveImageToInternalStorage

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.apply {
            setSupportActionBar(included.toolbar)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}