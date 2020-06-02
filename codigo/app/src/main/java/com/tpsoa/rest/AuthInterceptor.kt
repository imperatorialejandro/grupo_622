package com.tpsoa.rest

import com.tpsoa.sharedpreferences.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val token = SharedPreferencesManager.getToken()
        if (token!! != "") {
            requestBuilder.addHeader("token", token).build()
        }

        return chain.proceed(requestBuilder.build())
    }
}

