package com.example.networkanalyzer.model

data class VariableInsight(
    val requiredVariables: List<String>,
    val missingVariables: List<String>,
    val sensitiveVariables: List<String>,
    val extractionHints: Map<String, String>,
    val codeSnippets: Map<String, String>
)
