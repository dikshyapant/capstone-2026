package com.teammonarch.butterfly.data

// In-memory holder for the signed-in user's profile, so screens don't each
// need to re-fetch it. Cleared on logout. Fine for a single-session prototype;
// a production app would back this with a persisted/observable session store.
object AppSession {
    var currentProfile: ProfileRow? = null

    fun clear() {
        currentProfile = null
    }
}
