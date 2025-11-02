package com.example.networkanalyzer.ui.variables

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.networkanalyzer.databinding.ActivityVariableExtractorBinding

class VariableExtractorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVariableExtractorBinding
    private val viewModel: VariableExtractorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVariableExtractorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestId = intent.getLongExtra(EXTRA_REQUEST_ID, -1)
        if (requestId == -1L) {
            finish()
            return
        }

        observeVariables(requestId)
    }

    private fun observeVariables(id: Long) {
        viewModel.analyzeRequest(id).observe(this) { insight ->
            if (insight == null) {
                Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }
            binding.variableHeaderTextView.text = "Required Variables (${insight.requiredVariables.size})"
            binding.requiredVariablesTextView.text = insight.requiredVariables.joinToString("\n")
            binding.missingVariablesTextView.text = buildString {
                append("Missing variables:\n")
                append(insight.missingVariables.joinToString("\n").ifEmpty { "None" })
            }
            binding.extractionHintsTextView.text = insight.extractionHints.entries.joinToString("\n") {
                "${it.key}: ${it.value}"
            }
            binding.codeSnippetsTextView.text = insight.codeSnippets.entries.joinToString("\n\n") {
                "${it.key.uppercase()}\n${it.value}"
            }
        }
    }

    companion object {
        private const val EXTRA_REQUEST_ID = "extra_request_id"

        fun intent(context: Context, id: Long): Intent =
            Intent(context, VariableExtractorActivity::class.java).apply {
                putExtra(EXTRA_REQUEST_ID, id)
            }
    }
}
