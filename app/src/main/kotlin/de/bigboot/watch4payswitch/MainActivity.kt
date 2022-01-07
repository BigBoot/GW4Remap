package de.bigboot.watch4payswitch

import android.app.Activity
import android.os.Bundle
import de.bigboot.watch4payswitch.databinding.ActivityMainBinding
import android.content.Intent

import android.provider.Settings


class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchEnabled.setOnClickListener {
            openAccessibilitySettings()

            binding.switchEnabled.isChecked = checkAccesibilityServiceEnabled()
        }

    }

    override fun onResume() {
        super.onResume()

        binding.switchEnabled.isChecked = checkAccesibilityServiceEnabled()
    }

    private fun checkAccesibilityServiceEnabled(): Boolean {
        return Settings.Secure
            .getString(this.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("${packageName}/${AccessibilityService::class.qualifiedName}")
            ?: false

    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}