package dev.suli4.note.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickMedia = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                Log.d("Notes", "Selected URI: $uri")
            } else {
                Log.d("Notes", "No media selected")
            }
        }

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