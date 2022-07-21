package de.bigboot.gw4remap

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.Exception
import java.util.*

@JsonClass(generateAdapter = true)
data class ActivityTarget(val packageName: String, val activityName: String, val action: String? = null, val name: Int? = null)

@JsonClass(generateAdapter = false)
enum class ActivitySource(val text: Int)
{
    BUTTON_POWER_LONGPRESS(R.string.source_power_longpress),
    BUTTON_BACK_LONGPRESS(R.string.source_back_longpress),
}

@JsonClass(generateAdapter = true)
data class ActivityRule(val source: ActivitySource, val target: ActivityTarget, val id: UUID = UUID.randomUUID(), val enabled: Boolean = false)

private val KEY_FILE = "settings"
private val KEY_RULES = "rules"
private val KEY_REVISION = "revision"

class UuidJsonAdapter {
    @ToJson
    fun toJson(value: UUID?) = value?.toString()

    @FromJson
    fun fromJson(input: String): UUID = UUID.fromString(input)
}

class AppPreferences(context: Context) {
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
            .let {
                try {
                    it.fromJson(sharedPrefs.getString(KEY_RULES, "[]") ?: "") ?: emptyList()
                } catch (ex: Exception) {
                    emptyList()
                }
            }
            .toMutableList()
            .apply {
                if(size != 2 || none { it.source == ActivitySource.BUTTON_POWER_LONGPRESS } || none { it.source == ActivitySource.BUTTON_BACK_LONGPRESS }) {
                    clear()
                    add(ActivityRule(ActivitySource.BUTTON_POWER_LONGPRESS, PredefinedTargets.GOOGLE_ASSISTANT, enabled = true ))
                    add(ActivityRule(ActivitySource.BUTTON_BACK_LONGPRESS, PredefinedTargets.GOOGLE_PAY, enabled = true ))
                    saveRules(this)
                }
            }
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
        ?: ActivityRule(ActivitySource.BUTTON_POWER_LONGPRESS, ActivityTarget("", ""), id)

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

object PredefinedTargets {
    val GOOGLE_WALLET = ActivityTarget(
        "com.google.android.apps.walletnfcrel",
        "com.google.commerce.tapandpay.wear.cardlist.WalletThemedWearCardListActivity",
        name = R.string.target_google_wallet
    )

    val GOOGLE_ASSISTANT = ActivityTarget(
        "com.google.android.wearable.assistant",
        "com.google.android.wearable.assistant.MainActivity",
        action = "android.intent.action.ASSIST",
        name = R.string.target_google_assistant
    )

    val GOOGLE_ASSISTANT_GO = ActivityTarget(
        "com.google.android.apps.assistant",
        "com.google.android.apps.assistant.go.MainActivity",
        name = R.string.target_google_assistant_go
    )

    val ULTIMATE_ALEXA = ActivityTarget(
        "com.com.customsolutions.android.alexa",
        "com.customsolutions.android.alexa.MainActivity",
        name = R.string.target_ultimate_alexa
    )

    val ALL = listOf(GOOGLE_WALLET, GOOGLE_ASSISTANT, GOOGLE_ASSISTANT_GO, ULTIMATE_ALEXA)
}

fun Context.getAppPreferences() = AppPreferences(this)