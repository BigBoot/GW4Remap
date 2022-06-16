package de.bigboot.gw4remap

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.FragmentActivity
import de.bigboot.gw4remap.databinding.ActivityMainBinding
import java.util.*


class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchEnabled.setOnClickListener {
            openAccessibilitySettings()
        }

        for (rule in getAppPreferences().getRules()) {
            addRule(rule.id)
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceState()
    }

    private fun addRule(ruleID: UUID = UUID.randomUUID()) {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.layout_rules, ActivityRuleFragment.newInstance(ruleID).also { fragment ->
                fragment.onDelete = {
                    getAppPreferences().deleteRule(ruleID)
                    supportFragmentManager.beginTransaction().apply {
                        remove(fragment)
                    }.commitNow()
                }
            })
        }.commitNow()
    }


    private fun updateServiceState() {
        val serviceEnabled = checkAccesibilityServiceEnabled()

        binding.switchEnabled.visibility = when {
            serviceEnabled -> View.GONE
            else -> View.VISIBLE
        }

        binding.scrollviewRules.visibility = when {
            serviceEnabled -> View.VISIBLE
            else -> View.GONE
        }
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