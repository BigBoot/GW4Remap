package de.bigboot.gw4remap

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.widget.doOnTextChanged
import de.bigboot.gw4remap.databinding.FragmentActivityRuleBinding
import java.lang.IndexOutOfBoundsException
import java.util.*

private const val ARG_RULE_ID = "RULE_ID"

private abstract class SelectPredefined : ActivityResultContract<Unit?, String?>() {
    override fun parseResult(resultCode: Int, intent: Intent?) : String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getStringExtra(SelectPredefinedActivity.EXTRA_SELECTED_ACTIVITY) ?: ""
    }
}

private class SelectPredefinedTarget : SelectPredefined() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(context, SelectPredefinedActivity::class.java).apply {
            putExtra(SelectPredefinedActivity.EXTRA_ACTIVITY_TYPE, SelectPredefinedActivity.ActivityType.Target.name)
        }
}


class ActivityRuleFragment : Fragment() {
    var onDelete: (()->Unit)? = null

    private lateinit var ruleId: UUID
    private lateinit var binding: FragmentActivityRuleBinding

    private val selectPredefinedTarget = registerForActivityResult(SelectPredefinedTarget()) {
        it?.let {
            binding.texteditRuleTarget.setText(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ruleId = UUID.fromString(it.getString(ARG_RULE_ID))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityRuleBinding.inflate(inflater, container, false)

        val rule = context?.getAppPreferences()?.getRule(ruleId)

        if(rule != null)
        {
            binding.labelRuleSource.setText(rule.source.text)
            binding.texteditRuleTarget.setText("${rule.target.packageName}/${rule.target.activityName}/${rule.target.action}")
            binding.checkRuleEnabled.isChecked = rule.enabled

            binding.checkRuleEnabled.setOnCheckedChangeListener { _, _ -> saveRule() }
            binding.texteditRuleTarget.doOnTextChanged { _, _, _, _ -> saveRule() }

            binding.fabPredefinedTarget.setOnClickListener { selectPredefinedTarget.launch(null) }
        }

        return binding.root
    }

    private fun saveRule() {

        try {
            val rule = context?.getAppPreferences()?.getRule(ruleId)!!
            val (targetPackageName, targetActivityName, targetAction) = binding.texteditRuleTarget.text.toString()
                .split('/')
                .plus(arrayOf("",""))
                .take(3)

            context?.getAppPreferences()?.saveRule(rule.copy(
                source = rule.source,
                target = rule.target.copy(
                    packageName = targetPackageName,
                    activityName = targetActivityName,
                    action = targetAction,
                ),
                enabled = binding.checkRuleEnabled.isChecked
            ))
        } catch (_: IndexOutOfBoundsException) {}
    }

    companion object {
        @JvmStatic
        fun newInstance(ruleId: UUID = UUID.randomUUID()) =
            ActivityRuleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RULE_ID, ruleId.toString())
                }
            }
    }
}