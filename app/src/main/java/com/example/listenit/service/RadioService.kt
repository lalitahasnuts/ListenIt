package com.example.listenit.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object RadioService {
    // Конфигурация API
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Переменная для хранения токена аутентификации
    private var authToken: String? = null

    // Клиент OkHttp с интерцепторами
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            // Добавляем заголовок авторизации, если токен есть
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Access-Control-Allow-Origin", "http://localhost:8000")
                .addHeader("Access-Control-Allow-Credentials", "true")
                .addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")

            authToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit клиент
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(RadioApi::class.java)

    // Установка/очистка токена аутентификации
    fun setAuthToken(token: String?) {
        authToken = token
    }

    // Получение списка радиостанций
    suspend fun getRadio(): RadioResponse {
        return try {
            val response = api.getRadio()
            RadioResponse(response)
        } catch (e: Exception) {
            RadioResponse(emptyList(), "Ошибка при получении списка радио: ${e.message}")
        }
    }

    // Получение избранных радиостанций
    suspend fun getFavorites(): RadioResponse {
        return try {
            if (authToken == null) {
                throw IllegalStateException("Требуется аутентификация")
            }
            val response = api.getFavorites()
            RadioResponse(response)
        } catch (e: Exception) {
            RadioResponse(emptyList(), "Ошибка при получении избранного: ${e.message}")
        }
    }

    // Добавление радиостанции в избранное
    suspend fun addToFavorites(radioItem: RadioItem): RadioResponse {
        return try {
            if (authToken == null) {
                throw IllegalStateException("Требуется аутентификация")
            }
            val response = api.addFavorite(mapOf("name" to radioItem.name))
            RadioResponse(listOf(response))
        } catch (e: Exception) {
            RadioResponse(emptyList(), "Ошибка при добавлении в избранное: ${e.message}")
        }
    }

    // Удаление радиостанции из избранного
    suspend fun removeFromFavorites(radioItem: RadioItem): RadioResponse {
        return try {
            if (authToken == null) {
                throw IllegalStateException("Требуется аутентификация")
            }
            val response = api.removeFavorite(mapOf("name" to radioItem.name))
            RadioResponse(listOf(response))
        } catch (e: Exception) {
            RadioResponse(emptyList(), "Ошибка при удалении из избранного: ${e.message}")
        }
    }

    // Добавление пользовательской радиостанции
    suspend fun addCustomStation(name: String, url: String): RadioItem {
        return try {
            if (authToken == null) {
                throw IllegalStateException("Требуется аутентификация")
            }
            api.addRadio(mapOf(
                "name" to name,
                "source" to url
            ))
        } catch (e: Exception) {
            RadioItem("", "Ошибка при добавлении станции: ${e.message}", "")
        }
    }

    // Распознавание текущего трека (Shazam-like функционал)
    suspend fun recognizeSong(radioUrl: String): String {
        return try {
            if (authToken == null) {
                throw IllegalStateException("Требуется аутентификация")
            }
            api.recognizeSong(radioUrl)
        } catch (e: Exception) {
            "Ошибка распознавания: ${e.message}"
        }
    }

    // Интерфейс API
    private interface RadioApi {
        @GET("radio")
        suspend fun getRadio(): List<RadioItem>

        @GET("favorite")
        suspend fun getFavorites(): List<RadioItem>

        @POST("favorite")
        suspend fun addFavorite(@Body body: Map<String, String>): RadioItem

        @HTTP(method = "DELETE", path = "favorite", hasBody = true)
        suspend fun removeFavorite(@Body body: Map<String, String>): RadioItem

        @POST("radio")
        suspend fun addRadio(@Body body: Map<String, String>): RadioItem

        @GET("recognize")
        suspend fun recognizeSong(@Query("radio_url") radioUrl: String): String
    }
}

// Модели данных
data class RadioItem(
    val id: String,
    val name: String,
    val source: String
)

data class RadioResponse(
    val stations: List<RadioItem>,
    val error: String? = null
)
