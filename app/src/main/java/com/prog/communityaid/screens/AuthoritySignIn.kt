package com.prog.communityaid.screens

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.R
import com.prog.communityaid.auth.Auth
import com.prog.communityaid.data.repositories.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthoritySignIn : AppCompatActivity() {

    private lateinit var googleClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        auth = Firebase.auth
        val registerButton = findViewById<TextView>(R.id.signInButton)
        val emailField = findViewById<EditText>(R.id.emailInput)
        val passwordField = findViewById<EditText>(R.id.passwordInput)
        registerButton.setOnClickListener {
            val taskResult = auth.signInWithEmailAndPassword(
                emailField.text.toString(),
                passwordField.text.toString()
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    GlobalScope.launch {
                        val userRepo = UserRepository()
                        val result = userRepo.findWhereAsync(
                            userRepo.collection.whereEqualTo(
                                "email",
                                user?.email
                            )
                        ).await()
                        Auth.user = result[0]
                        val intent = Intent(applicationContext, Feed::class.java)
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

}