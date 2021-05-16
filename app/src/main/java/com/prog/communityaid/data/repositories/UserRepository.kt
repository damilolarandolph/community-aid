package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.data.models.User

class UserRepository : Repository<User>(Firebase.firestore.collection("users")) {


    override fun hydrator(doc: Map<String, Any>): User {
        var user = User()
        user.email = doc["email"] as String
        user.name = doc["name"] as String
        user.id = doc["id"] as String
        user.userType = if (doc["userType"] != null) doc["userType"] as String else "user"
        return user
    }

}