package com.example.stockmanager.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {
    val client =
        createSupabaseClient(
            supabaseUrl = "https://kavoacbirloowjxscpeg.supabase.co",
            supabaseKey = "sb_publishable_aEIZmFrQnChtFGJg-swY8A_gqAhCWE6",
        ) {
            install(Postgrest)
            install(Realtime)
        }
}
