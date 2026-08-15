package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class FaceTimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching {
            FirebaseApp.initializeApp(this)
            Log.d("FaceTimeApplication", "Firebase initialized successfully")
        }.onFailure {
            Log.e("FaceTimeApplication", "Firebase initialization note: ${it.message}")
        }
    }
}
