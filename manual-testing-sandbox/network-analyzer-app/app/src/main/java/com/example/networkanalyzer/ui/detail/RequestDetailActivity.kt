package com.example.networkanalyzer.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.networkanalyzer.databinding.ActivityRequestDetailBinding
import com.example.networkanalyzer.service.CaptureInterceptor
import com.example.networkanalyzer.ui.variables.VariableExtractorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestDetailBinding
    private val viewModel: RequestDetailViewModel by viewModels()
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(CaptureInterceptor())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestId = intent.getLongExtra(EXTRA_REQUEST_ID, -1)
        if (requestId == -1L) {
            finish()
            return
        }

        observeRequest(requestId)
    }

    private fun observeRequest(id: Long) {
        viewModel.loadRequest(id).observe(this) { request ->
            if (request == null) {
                Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }
            binding.detailUrlTextView.text = request.url
            binding.detailMethodTextView.text = request.method
            binding.detailStatusTextView.text = request.responseCode?.toString() ?: "--"
            binding.detailHeadersTextView.text = request.requestHeaders
            binding.detailRequestBodyTextView.text = request.requestBody ?: ""
            binding.detailResponseBodyTextView.text = request.responseBody ?: ""

            binding.replayButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val mediaTypeValue = request.requestHeaders.lines()
                        .firstOrNull { it.startsWith("Content-Type", ignoreCase = true) }
                        ?.substringAfter(":")
                        ?.trim()
                    val mediaType = mediaTypeValue?.toMediaTypeOrNull()
                    val body = request.requestBody?.toRequestBody(mediaType)
                    val normalizedMethod = request.method.uppercase()
                    val methodBody = when (normalizedMethod) {
                        "GET", "HEAD" -> null
                        else -> body
                    }
                    val okRequestBuilder = Request.Builder()
                        .url(request.url)
                        .method(request.method, methodBody)
                    request.requestHeaders.lines().forEach { headerLine ->
                        val separatorIndex = headerLine.indexOf(":")
                        if (separatorIndex > 0) {
                            val name = headerLine.substring(0, separatorIndex).trim()
                            val value = headerLine.substring(separatorIndex + 1).trim()
                            if (!name.equals("Content-Length", ignoreCase = true)) {
                                okRequestBuilder.addHeader(name, value)
                            }
                        }
                    }
                    val okRequest = okRequestBuilder.build()
                    runCatching { okHttpClient.newCall(okRequest).execute() }
                        .onSuccess {
                            launch(Dispatchers.Main) {
                                Toast.makeText(this@RequestDetailActivity, "Request replayed", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .onFailure { throwable ->
                            launch(Dispatchers.Main) {
                                Toast.makeText(
                                    this@RequestDetailActivity,
                                    "Replay failed: ${throwable.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

            binding.openVariablesButton.setOnClickListener {
                startActivity(VariableExtractorActivity.intent(this, id))
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST_ID = "extra_request_id"

        fun intent(context: Context, id: Long): Intent =
            Intent(context, RequestDetailActivity::class.java).apply {
                putExtra(EXTRA_REQUEST_ID, id)
            }
    }
}
