package com.prog.communityaid.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.R
import com.prog.communityaid.auth.Auth
import com.prog.communityaid.data.repositories.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            val isUser = Firebase.auth.currentUser != null
            val user = Firebase.auth.currentUser
            val userRepo = UserRepository()
            if (!isUser) {
                val intent = Intent(applicationContext, SignIn::class.java)
                startActivity(intent)
            } else {
                GlobalScope.launch {
                    val result = userRepo.findWhereAsync(
                        userRepo.collection.whereEqualTo(
                            "email",
                            user?.email
                        )
                    ).await()
                    Auth.user = result[0]
                    runOnUiThread {
                        val intent = Intent(applicationContext, Feed::class.java)
                        startActivity(intent)
                    }
                }

            }
        }


    }
}
