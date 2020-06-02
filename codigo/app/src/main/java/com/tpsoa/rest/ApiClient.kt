package com.tpsoa.rest

import com.tpsoa.model.*
import com.tpsoa.sharedpreferences.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object ApiConstants {
    const val CONTENT_TYPE_HEADER = "Content-Type:application/json"
}


interface ApiInterface {
    @Headers(ApiConstants.CONTENT_TYPE_HEADER)
    @POST("login")
    fun SignIn(@Body info: SignInRequest): Call<SignInResponse>

    @Headers(ApiConstants.CONTENT_TYPE_HEADER)
    @POST("register")
    fun SignUp(@Body info: SignUpRequest): Call<SignUpResponse>

    @Headers(ApiConstants.CONTENT_TYPE_HEADER)
    @POST("event")
    fun RegisterEvent(@Body info: EventRequest): Call<EventResponse>
}

object ServiceBuilder {
    private const val BASE_URL: String = "http://so-unlam.net.ar/api/api/"

    private val client: OkHttpClient = OkHttpClient.Builder().apply {
        this.addInterceptor(AuthInterceptor())
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
