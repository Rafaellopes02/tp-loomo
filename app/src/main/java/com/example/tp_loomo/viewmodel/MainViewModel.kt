package com.example.tp_loomo.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("offline_prefs", Context.MODE_PRIVATE)

    @Serializable
    data class RoleResponse(val role: String)

    var currentRole by mutableStateOf("loading")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchUserRole() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId == null) {
                    currentRole = "user"
                    return@launch
                }

                val response = supabase.postgrest["profiles"]
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("role")) {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<RoleResponse>()

                val role = response?.role ?: "user"

                // Guarda em cache para uso offline
                prefs.edit().putString("cached_role", role).apply()

                currentRole = role
                android.util.Log.d("NAV_DEBUG", "Role carregado: '$currentRole'")

            } catch (e: Exception) {
                // Sem rede — usa o cache
                val cachedRole = prefs.getString("cached_role", null)
                currentRole = cachedRole ?: "user"
                android.util.Log.e("NAV_DEBUG", "Sem rede, role em cache: '$currentRole'")
            }
        }
    }
}