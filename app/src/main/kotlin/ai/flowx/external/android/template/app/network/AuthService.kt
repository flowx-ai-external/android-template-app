package ai.flowx.external.android.template.app.network

import ai.flowx.external.android.template.app.Env
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// https://auth-33.demo.flowx.ai/auth/realms/flowx/.well-known/openid-configuration
interface AuthService {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("client_id") clientId: String = Env.clientId,
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
    ): LoginResponse

    @FormUrlEncoded
    @POST("logout")
    suspend fun logout(
        @Field("client_id") clientId: String = Env.clientId,
        @Field("refresh_token") refreshToken: String,
    ): Response<Unit>
}

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String,
)