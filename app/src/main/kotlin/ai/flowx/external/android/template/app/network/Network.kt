package ai.flowx.external.android.template.app.network

import ai.flowx.external.android.template.app.Env
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Network {
    private val authRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Env.authBaseUrl)
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        authRetrofit.create(AuthService::class.java)
    }
}