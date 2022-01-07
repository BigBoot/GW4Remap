package de.bigboot.watch4payswitch

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AccessibilityService : AccessibilityService() {

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
        Log.v(this::class.simpleName, "Active package changed: ${event.packageName}")

        if(event.packageName == getString(R.string.source_package_name))
        {
            startActivity(Intent().apply {
                setClassName(
                    getString(R.string.target_package_name),
                    getString(R.string.target_activity)
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })

        }
    }
}
