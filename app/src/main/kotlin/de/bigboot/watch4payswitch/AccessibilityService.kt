package de.bigboot.gw4remap

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.util.*

class AccessibilityService : AccessibilityService() {
    private var quickPanelVisible = false

    private var rules: List<ActivityRule> = emptyList()
    private var rulesRevision: UUID = UUID.randomUUID()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.v(this::class.simpleName, "AccessibilityService connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v(this::class.simpleName, "AccessibilityService disconnected")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.v(this::class.simpleName, event.toString())

        if(event.packageName == "com.google.android.apps.wearable.systemui")
        {
            if(event.contentChangeTypes and AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_APPEARED != 0)
            {
                quickPanelVisible = true
            }

            if(event.contentChangeTypes and AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED != 0)
            {
                quickPanelVisible = false
            }
        }

        getAppPreferences().let { prefs ->
            if(prefs.revision() != rulesRevision) {
                rulesRevision = prefs.revision()
                rules = prefs.getRules()
            }
        }

        val rule = rules.firstOrNull {
            it.source.packageName == event.packageName ||
                    (it.source.packageName == PredefinedSources.POWER_MENU.packageName
                            && !quickPanelVisible
                            && event.packageName == "com.google.android.apps.wearable.systemui"
                            && event.className == "com.google.android.clockwork.systemui.globalactions.dialog.GlobalActionDialog")
        }

        if(rule != null)
        {
            if(rule.source.packageName == PredefinedSources.POWER_MENU.packageName)
            {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }

            startActivity(Intent().apply {
                setClassName(rule.target.packageName, rule.target.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}
