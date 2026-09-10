package com.teammonarch.butterfly.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    // Public project URL + anon key -- safe to embed in a client app; access is
    // controlled by Row Level Security policies in the database, not by keeping
    // this secret.
    private const val SUPABASE_URL = "https://vqiwmlptbidbvrpdhifc.supabase.co"
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZxaXdtbHB0YmlkYnZycGRoaWZjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg5MDA3NDAsImV4cCI6MjEwNDQ3Njc0MH0.6vGpBVwQzi0ohwPIzNpBN6CqgxxoEA8SRtokfEPu54I"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
