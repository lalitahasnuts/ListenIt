package com.example.listenit.service

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

// Классы запросов и ответов
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
)

data class LoginResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("token_type") val token_type: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UserRead(
    val id: Int,
    val email: String
)

// Интерфейс API
interface AuthApi {
    @FormUrlEncoded
    @POST("auth/jwt/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): UserRead
}

// Сервис аутентификации
object AuthService {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Токен будет храниться здесь после успешной аутентификации
    private var authToken: String? = null

    // Интерцептор для добавления заголовка Authorization
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Добавляем заголовок только если токен есть
        authToken?.let { token ->
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

        chain.proceed(originalRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor) // Добавляем наш интерцептор
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private val api = retrofit.create(AuthApi::class.java)

    suspend fun login(email: String, password: String): LoginResponse {
        val response = api.login(username = email, password = password)
        // Сохраняем токен после успешного входа
        authToken = response.access_token
        return response
    }

    suspend fun register(email: String, password: String): UserRead {
        return api.register(RegisterRequest(email = email, password = password))
    }

    // Функция для установки токена (если он получен извне)
    fun setAuthToken(token: String) {
        authToken = token
    }

    // Функция для очистки токена (при выходе)
    fun clearAuthToken() {
        authToken = null
    }
}

