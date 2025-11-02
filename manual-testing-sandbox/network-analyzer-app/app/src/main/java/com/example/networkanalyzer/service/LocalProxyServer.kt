package com.example.networkanalyzer.service

import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.util.TrafficLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.toRequestBody

class LocalProxyServer(
    private val port: Int,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null

    fun start() {
        if (serverSocket != null) return
        scope.launch {
            serverSocket = ServerSocket(port)
            while (true) {
                val socket = runCatching { serverSocket?.accept() }.getOrNull() ?: break
                handleClient(socket)
            }
        }
    }

    fun stop() {
        scope.cancel()
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    private fun handleClient(socket: Socket) {
        scope.launch {
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val writer = PrintWriter(s.getOutputStream(), true)
                val requestLine = reader.readLine() ?: return@use
                val requestParts = requestLine.split(" ")
                if (requestParts.size < 2) return@use
                val method = requestParts[0]
                val url = requestParts[1]

                val headerBuilder = Headers.Builder()
                var line: String?
                val headersBuffer = StringBuilder()
                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: break
                    if (l.isBlank()) break
                    headersBuffer.appendLine(l)
                    val splitIndex = l.indexOf(":")
                    if (splitIndex > 0) {
                        val name = l.substring(0, splitIndex).trim()
                        val value = l.substring(splitIndex + 1).trim()
                        headerBuilder.add(name, value)
                    }
                }

                val bodyBuilder = StringBuilder()
                while (reader.ready()) {
                    bodyBuilder.append(reader.readLine())
                }
                val requestBody = if (bodyBuilder.isEmpty()) null else bodyBuilder.toString()

                val okhttpRequestBuilder = Request.Builder()
                    .url(url)
                val requestBodyInstance = requestBody?.toRequestBody(null)
                val normalizedMethod = method.uppercase()
                val methodBody = when (normalizedMethod) {
                    "GET", "HEAD" -> null
                    else -> requestBodyInstance
                }
                okhttpRequestBuilder.method(method, methodBody)
                headerBuilder.build().forEach { okhttpRequestBuilder.addHeader(it.first, it.second) }

                val start = System.currentTimeMillis()
                val response = runCatching { client.newCall(okhttpRequestBuilder.build()).execute() }.getOrNull()
                val end = System.currentTimeMillis()
                val responseBody = response?.body?.string()
                val responseHeaders = response?.headers?.toMultimap()?.entries?.joinToString("\n") { "${it.key}: ${it.value.joinToString()}" }

                writer.println("HTTP/1.1 ${response?.code ?: 500} ${response?.message ?: "Internal Proxy Error"}")
                response?.headers?.forEach { header ->
                    writer.println("${header.first}: ${header.second}")
                }
                writer.println()
                responseBody?.let { writer.println(it) }
                writer.flush()

                val networkRequest = NetworkRequest(
                    timestamp = System.currentTimeMillis(),
                    method = method,
                    url = url,
                    requestHeaders = headersBuffer.toString(),
                    requestBody = requestBody,
                    responseCode = response?.code,
                    responseHeaders = responseHeaders,
                    responseBody = responseBody,
                    isHttps = url.startsWith("https", ignoreCase = true),
                    durationMillis = end - start
                )
                TrafficLogger.logRequest(networkRequest)
                response?.close()
            }
        }
    }
}
