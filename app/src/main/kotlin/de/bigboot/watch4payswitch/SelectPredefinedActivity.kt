package de.bigboot.watch4payswitch

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import de.bigboot.watch4payswitch.databinding.ActivitySelectPredefinedBinding

class SelectPredefinedActivity : AppCompatActivity() {
    enum class ActivityType { Source, Target }

    private lateinit var binding: ActivitySelectPredefinedBinding
    private lateinit var activityType: ActivityType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityType = ActivityType.valueOf(
            intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: ActivityType.Source.name
        )

        binding = ActivitySelectPredefinedBinding.inflate(layoutInflater)

        binding.recyclerView.layoutManager = WearableLinearLayoutManager(this)
        binding.recyclerView.adapter = SelectPredefinedAdapter(when (activityType) {
            ActivityType.Source -> PredefinedSources.ALL.map { source ->
                SelectPredefinedAdapter.Item(
                    source.name?.let { getText(it).toString() } ?: "",
                    source.packageName)
            }
            ActivityType.Target -> PredefinedTargets.ALL.map { target ->
                SelectPredefinedAdapter.Item(
                    target.name?.let { getText(it).toString() } ?: "",
                    "${target.packageName}/${target.activityName}")
            }
        }).apply {
            onItemSelected = {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_SELECTED_ACTIVITY, it.value)
                })
                finish()
            }
        }

        setContentView(binding.root)
    }

    companion object {
        val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"
        val EXTRA_SELECTED_ACTIVITY = "EXTRA_SELECTED_ACTIVITY"
    }
}