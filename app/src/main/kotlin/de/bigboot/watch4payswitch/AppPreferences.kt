package de.bigboot.watch4payswitch

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

@JsonClass(generateAdapter = true)
data class ActivityTarget(val packageName: String, val activityName: String, val name: Int? = null)

@JsonClass(generateAdapter = true)
data class ActivitySource(val packageName: String, val name: Int? = null)

@JsonClass(generateAdapter = true)
data class ActivityRule(val source: ActivitySource, val target: ActivityTarget, val id: UUID = UUID.randomUUID())

private val KEY_FILE = "settings"
private val KEY_RULES = "rules"
private val KEY_REVISION = "revision"

class UuidJsonAdapter {
    @ToJson
    fun toJson(value: UUID?) = value?.toString()

    @FromJson
    fun fromJson(input: String): UUID = UUID.fromString(input)
}

class AppPreferences(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences(KEY_FILE, Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
        .add(UuidJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getRules(): List<ActivityRule> {
        return moshi
            .adapter<List<ActivityRule>>(
                Types.newParameterizedType(
                    List::class.java,
                    ActivityRule::class.java
                )
            )
            .lenient()
            .fromJson(sharedPrefs.getString(KEY_RULES, "[]") ?: "")
            ?: emptyList()
    }

    private fun saveRules(rules: List<ActivityRule>) {
        sharedPrefs.edit {
            putString(
                KEY_RULES, moshi
                    .adapter<List<ActivityRule>>(
                        Types.newParameterizedType(
                            List::class.java,
                            ActivityRule::class.java
                        )
                    )
                    .toJson(rules)
            )

            putString(KEY_REVISION, moshi.adapter(UUID::class.java).toJson(UUID.randomUUID()))
        }
    }

    fun getRule(id: UUID): ActivityRule = getRules().find { it.id == id }
        ?: ActivityRule(ActivitySource(""), ActivityTarget("", ""), id)

    fun saveRule(rule: ActivityRule) {
        saveRules(getRules().filter { it.id != rule.id } + rule)
    }

    fun deleteRule(id: UUID) {
        saveRules(getRules().filter { it.id != id })
    }

    fun revision(): UUID = moshi.adapter(UUID::class.java)
        .lenient()
        .fromJson(sharedPrefs.getString(KEY_REVISION, UUID.randomUUID().toString()) ?: "{}")
        ?: UUID.randomUUID()
}

object PredefinedSources {
    val POWER_MENU = ActivitySource(
        "POWER_MENU",
        R.string.source_power_menu
    )

    val SAMSUNG_PAY = ActivitySource(
        "com.samsung.android.samsungpay.gear",
        R.string.source_samsung_pay
    )

    val BIXBY = ActivitySource(
        "com.samsung.android.bixby.agent",
        R.string.source_bixby
    )

    val ALL = listOf(POWER_MENU, SAMSUNG_PAY, BIXBY)
}

object PredefinedTargets {
    val GOOGLE_PAY = ActivityTarget(
        "com.google.android.apps.walletnfcrel",
        "com.google.commerce.tapandpay.android.wearable.cardlist.WearPayActivity",
        R.string.target_google_pay
    )

    val GOOGLE_ASSISTANT_GO = ActivityTarget(
        "com.google.android.apps.assistant",
        "com.google.android.apps.assistant.go.MainActivity",
        R.string.target_google_assistant_go
    )

    val ALL = listOf(GOOGLE_PAY, GOOGLE_ASSISTANT_GO)
}

fun Context.getAppPreferences() = AppPreferences(this)