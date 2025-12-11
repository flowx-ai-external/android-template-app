package ai.flowx.external.android.template.app

// TODO SETUP: configure your environment here by setting the appropriate values
object Env {
    // Platform data:
    const val baseUrl = "your_base_url"
    const val imageBaseUrl = "your_image_base_url"
    const val enginePath = "your_engine_path"

    // Keycloak data:
    const val authBaseUrl = "your_authentication_url" // should contain the `/protocol/openid-connect` suffix as path's last segments
    const val clientId = "your_client_id"
}