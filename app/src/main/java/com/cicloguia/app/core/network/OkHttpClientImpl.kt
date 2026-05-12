package com.cicloguia.app.core.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class OkHttpClientImpl @Inject constructor(
    private val client: OkHttpClient
) : HttpClient {

    override suspend fun get(url: String): String = suspendCancellableCoroutine { cont ->
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)

        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        cont.resumeWith(Result.failure(IOException("HTTP ${it.code}")))
                    } else {
                        cont.resumeWith(Result.success(it.body.string()))
                    }
                }
            }
        })
    }
}