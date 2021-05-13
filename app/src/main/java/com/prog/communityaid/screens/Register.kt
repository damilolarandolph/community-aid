package com.prog.communityaid.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.R
import com.prog.communityaid.auth.Auth
import com.prog.communityaid.data.models.User
import com.prog.communityaid.data.repositories.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private fun signIn() {
        val signInIntent = this.googleClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.e(TAG, account.toString())
                Log.e(TAG, "firebaseAuthWithGoogle" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val userRepo = UserRepository()
                    GlobalScope.launch {
                        val result = userRepo.findWhere(
                            userRepo.collection.whereEqualTo(
                                "email",
                                user?.email
                            )
                        )
                            .await()
                        if (result.isNotEmpty()) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "User already registered, sign in !",
                                    Toast.LENGTH_LONG
                                ).show()
                                auth.signOut()
                            }

                        } else {
                            val dbUser = User()
                            dbUser.email = user?.email!!
                            dbUser.name = user.displayName!!
                            userRepo.save(dbUser).await()
                            Auth.user = dbUser
                            val intent = Intent(applicationContext, Feed::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_select)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                "205305548761-etgoganvsjsnmgh0bvjj45aj638ss266.apps.googleusercontent.com"
            )
            .requestEmail()
            .requestProfile()
            .build()
        this.googleClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        val signInText = findViewById<TextView>(R.id.signinText)
        val googleButton = findViewById<Button>(R.id.googleButton)
        signInText.setOnClickListener {
            val intent = Intent(applicationContext, SignIn::class.java)
            startActivity(intent)
        }

        googleButton.setOnClickListener {
            this.signIn()
        }
    }
}