package com.tpsoa.rest

import com.tpsoa.model.SignInRequest
import com.tpsoa.model.SignUpRequest
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object ApiConstants {
    const val CONTENT_TYPE_HEADER = "Content-Type:application/json"
}

data class SignInResponse(
    val state: String,
    val msg: String,
    val token: String
)

data class SignUpResponse(
    val state: String,
    val env: String,
    val token: String,
    val msg: String
)

interface ApiInterface {
    @Headers(ApiConstants.CONTENT_TYPE_HEADER)
    @POST("login")
    fun SignIn(@Body info: SignInRequest): Call<SignInResponse>

    @Headers(ApiConstants.CONTENT_TYPE_HEADER)
    @POST("register")
    fun SignUp(@Body info: SignUpRequest): Call<SignUpResponse>
}

object ServiceBuilder {
    private const val BASE_URL: String = "http://so-unlam.net.ar/api/api/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}