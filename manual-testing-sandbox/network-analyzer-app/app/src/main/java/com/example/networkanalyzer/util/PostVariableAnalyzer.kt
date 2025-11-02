package com.example.networkanalyzer.util

import android.net.Uri
import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.model.VariableInsight
import com.google.gson.JsonParser

object PostVariableAnalyzer {
    private val sensitiveKeywords = listOf("token", "csrf", "session", "auth", "password")

    fun analyze(request: NetworkRequest): VariableInsight {
        val bodyMap = mutableMapOf<String, String>()
        request.requestBody?.let { body ->
            if (body.startsWith("{")) {
                runCatching {
                    val json = JsonParser.parseString(body).asJsonObject
                    json.entrySet().forEach { (key, value) ->
                        bodyMap[key] = value.toString()
                    }
                }
            } else {
                val pairs = body.split('&')
                pairs.forEach { pair ->
                    val parts = pair.split('=')
                    if (parts.isNotEmpty()) {
                        val key = Uri.decode(parts[0])
                        val value = if (parts.size > 1) Uri.decode(parts[1]) else ""
                        bodyMap[key] = value
                    }
                }
            }
        }

        val headerMap = request.requestHeaders.lines().mapNotNull { line ->
            line.split(": ").takeIf { it.size == 2 }?.let { it[0] to it[1] }
        }.toMap()

        val requiredVariables = bodyMap.keys.ifEmpty { headerMap.keys }.toList()
        val missingVariables = requiredVariables.filter { bodyMap[it].isNullOrEmpty() }
        val sensitiveVariables = requiredVariables.filter { key ->
            sensitiveKeywords.any { keyword -> key.contains(keyword, ignoreCase = true) }
        }

        val hints = mutableMapOf<String, String>()
        requiredVariables.forEach { variable ->
            val hint = when {
                variable.contains("csrf", ignoreCase = true) -> "Check hidden form fields or JavaScript assignment scripts."
                variable.contains("token", ignoreCase = true) -> "Tokens often appear in JSON responses or Authorization headers."
                variable.contains("session", ignoreCase = true) -> "Session IDs are usually stored in cookies."
                variable.contains("password", ignoreCase = true) -> "Prompt user input securely; avoid logging plain text."
                else -> "Trace previous requests or responses for this parameter."
            }
            hints[variable] = hint
        }

        val codeSnippets = buildMap {
            put(
                "curl",
                "curl '${request.url}' -X ${request.method} \\\n  -H 'Content-Type: application/x-www-form-urlencoded' \\\n  --data '${request.requestBody ?: ""}'"
            )
            put(
                "python",
                "import requests\n\ns = requests.Session()\nresponse = s.post('${request.url}', data={${bodyMap.entries.joinToString { "'${it.key}': '${it.value}'" }}})\nprint(response.text)"
            )
            put(
                "bash",
                "http --session=analysis ${request.url} ${bodyMap.entries.joinToString(" ") { "${it.key}==${it.value}" }}"
            )
        }

        return VariableInsight(
            requiredVariables = requiredVariables,
            missingVariables = missingVariables,
            sensitiveVariables = sensitiveVariables,
            extractionHints = hints,
            codeSnippets = codeSnippets
        )
    }
}
