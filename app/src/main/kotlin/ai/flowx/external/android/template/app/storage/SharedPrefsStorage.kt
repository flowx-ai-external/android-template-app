package ai.flowx.external.android.template.app.storage

import android.content.Context

class SharedPrefsStorage(private val context: Context) {

    private val sharedPrefs by lazy {
        context.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String) = run { sharedPrefs.edit().putString(key, value).apply() }
    fun getString(key: String): String? = sharedPrefs.getString(key, null)
    fun clear() = run { sharedPrefs.edit().clear().apply() }

    companion object {
        const val AUTH_ACCESS_TOKEN_PREF = "access_token"
        const val AUTH_REFRESH_TOKEN_PREF = "refresh_token"
    }
}