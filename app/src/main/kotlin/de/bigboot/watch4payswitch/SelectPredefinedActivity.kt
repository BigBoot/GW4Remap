package de.bigboot.gw4remap

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import de.bigboot.gw4remap.databinding.ActivitySelectPredefinedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)

                val index = parent.getChildAdapterPosition(view)
                val count = parent.adapter?.itemCount?.minus(1) ?: 0

                if (index == 0) {
                    outRect.top =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            50.0f,
                            resources.displayMetrics
                        )
                            .toInt()
                }

                if (index == count) {
                    outRect.bottom =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            50.0f,
                            resources.displayMetrics
                        )
                            .toInt()
                }
            }
        })
        binding.recyclerView.adapter = SelectPredefinedAdapter(predefinedTargetItems()).apply {
            onItemSelected = {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_SELECTED_ACTIVITY, it.value)
                })
                finish()
            }

            if (activityType == ActivityType.Target) {
                loadAllApps(this)
            }
        }


        setContentView(binding.root)
    }


    private fun loadAllApps(targetAdapter: SelectPredefinedAdapter) {
        lifecycleScope.launch(Dispatchers.Default) {
            val targetIntent = Intent(Intent.ACTION_MAIN)
                .also { it.addCategory(Intent.CATEGORY_LAUNCHER) }

            val apps = packageManager.queryIntentActivities(targetIntent, 0)
                .map { resolveInfo ->
                    val label = resolveInfo.loadLabel(packageManager)

                    SelectPredefinedAdapter.Item(
                        label.toString(),
                        "${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}"
                    )
                }
                .sortedBy { it.name }

            val predefinedAndApps = predefinedTargetItems() + apps

            withContext(Dispatchers.Main) {
                targetAdapter.items = predefinedAndApps
            }
        }
    }

    private fun predefinedTargetItems() = PredefinedTargets.ALL.map { target ->
        SelectPredefinedAdapter.Item(
            target.name?.let { getText(it).toString() } ?: "",
            "${target.packageName}/${target.activityName}/${target.action}")
    }


    companion object {
        val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"
        val EXTRA_SELECTED_ACTIVITY = "EXTRA_SELECTED_ACTIVITY"
    }
}
