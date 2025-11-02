package com.example.networkanalyzer.service

import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.util.TrafficLogger
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class CaptureInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.currentTimeMillis()
        val response = chain.proceed(request)
        val end = System.currentTimeMillis()

        val requestBody = request.body?.let { body ->
            val copy = body
            val buffer = Buffer()
            copy.writeTo(buffer)
            buffer.readUtf8()
        }

        val responseBodyString = response.peekBody(Long.MAX_VALUE).string()

        val networkRequest = NetworkRequest(
            timestamp = System.currentTimeMillis(),
            method = request.method,
            url = request.url.toString(),
            requestHeaders = request.headers.joinToString("\n") { "${it.first}: ${it.second}" },
            requestBody = requestBody,
            responseCode = response.code,
            responseHeaders = response.headers.joinToString("\n") { "${it.first}: ${it.second}" },
            responseBody = responseBodyString,
            isHttps = request.isHttps,
            durationMillis = end - start
        )
        TrafficLogger.logRequest(networkRequest)
        return response
    }
}
