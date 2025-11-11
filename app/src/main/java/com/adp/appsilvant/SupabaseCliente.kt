package com.adp.appsilvant

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseCliente {

    private const val SUPABASE_URL = "https://kvaasozqxayjxzqvmpav.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_ikyzl81YpGrsFwhxq6UoTw_UzrT6wnj"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest) {
            serializer = KotlinXSerializer(Json { 
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Storage)
        install(Realtime)
    }
}