package de.bigboot.gw4remap

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.getSystemService
import kotlinx.coroutines.*
import java.util.*
import java.util.regex.Pattern


class AccessibilityService : AccessibilityService() {
    private var rules: List<ActivityRule> = emptyList()
    private var rulesRevision: UUID = UUID.randomUUID()

    private var logcatWatcher: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.v(this::class.simpleName, "AccessibilityService connected")

        logcatWatcher = GlobalScope.launch(Dispatchers.IO) {
            Runtime.getRuntime()
                .exec(arrayOf("logcat", "-T", "1", "-b", "system", "-e", "stemPrimaryLongPress|powerLongPress"))
                .inputStream
                .use { reader ->
                    Log.d(AccessibilityService::class.simpleName, "watching logcat started...")
                    val buffer = ByteArray(1024 * 10) { 0 }
                    while (true) {
                        val read = reader.read(buffer, 0, buffer.size)
                        if(read <= 0) break

                        val line = String(buffer, 0, read)
                        Log.d(AccessibilityService::class.simpleName, String(buffer, 0, read))
                        when {
                            line.contains("stemPrimaryLongPress") -> onActivitySource(ActivitySource.BUTTON_BACK_LONGPRESS)
                            line.contains("powerLongPress") ->onActivitySource(ActivitySource.BUTTON_POWER_LONGPRESS)
                            else -> {}
                        }
                    }
                    Log.d(AccessibilityService::class.simpleName, "watching logcat exited...")
                }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        runBlocking { logcatWatcher?.cancelAndJoin() }
        Log.v(this::class.simpleName, "AccessibilityService disconnected")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    private fun onActivitySource(source: ActivitySource) {
        getAppPreferences().let { prefs ->
            if(prefs.revision() != rulesRevision) {
                rulesRevision = prefs.revision()
                rules = prefs.getRules()
            }
        }

        val rule = rules.firstOrNull { it.enabled && it.source == source }

        if(rule != null)
        {
            getSystemService<Vibrator>()?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
            try {
                startActivity(Intent().apply {
                    setClassName(rule.target.packageName, rule.target.activityName)
                    action = rule.target.action
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (ex: Throwable) {
                Log.w(AccessibilityService::class.simpleName, "Unable to start activity", ex)
            }
        }
    }
}
