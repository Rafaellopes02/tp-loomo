package com.example.tp_loomo.data.remote.api

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage


val supabase = createSupabaseClient(
    supabaseUrl = "https://gahdheovdstkvkqbafju.supabase.co",
    supabaseKey = "sb_publishable_PPKbpyGUJtSUjlqelvgNvA_NtR5yusk"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}