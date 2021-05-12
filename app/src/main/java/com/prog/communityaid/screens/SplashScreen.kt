package com.prog.communityaid.screens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.R

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this);
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val vidView = findViewById<VideoView>(R.id.videoView)
        val path = "android.resource://" + this.packageName + "/" + R.raw.logo
        vidView.setVideoURI(Uri.parse(path))
        vidView.start()
        vidView.setOnCompletionListener { mp ->
            mp.seekTo(0)
            mp.start()
            mp.isLooping = true
         val isUser =   Firebase.auth.currentUser == null ;
        }


    }
}
