package de.bigboot.watch4payswitch

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
import de.bigboot.watch4payswitch.databinding.FragmentActivityRuleBinding
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

private class SelectPredefinedSource : SelectPredefined() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(context, SelectPredefinedActivity::class.java).apply {
            putExtra(SelectPredefinedActivity.EXTRA_ACTIVITY_TYPE, SelectPredefinedActivity.ActivityType.Source.name)
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

    private val selectPredefinedSource = registerForActivityResult(SelectPredefinedSource()) {
        it?.let {
            binding.texteditRuleSource.setText(it)
        }
    }

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
            binding.texteditRuleSource.setText(rule.source.packageName)
            binding.texteditRuleTarget.setText("${rule.target.packageName}/${rule.target.activityName}")

            binding.texteditRuleSource.doOnTextChanged { _, _, _, _ -> saveRule() }
            binding.texteditRuleTarget.doOnTextChanged { _, _, _, _ -> saveRule() }

            binding.fabPredefinedSource.setOnClickListener { selectPredefinedSource.launch(null) }
            binding.fabPredefinedTarget.setOnClickListener { selectPredefinedTarget.launch(null) }
        }

        binding.buttonDeleteRule.setOnClickListener {
            onDelete?.invoke()
        }

        return binding.root
    }

    private fun saveRule() {

        try {
            val rule = context?.getAppPreferences()?.getRule(ruleId)!!
            val sourcePackageName = binding.texteditRuleSource.text.toString()
            val (targetPackageName, targetActivityName) = binding.texteditRuleTarget.text.toString()
                .split('/').take(2)

            context?.getAppPreferences()?.saveRule(rule.copy(
                source = rule.source.copy(
                    packageName = sourcePackageName.toString()
                ),
                target = rule.target.copy(
                    packageName = targetPackageName,
                    activityName = targetActivityName
                )
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