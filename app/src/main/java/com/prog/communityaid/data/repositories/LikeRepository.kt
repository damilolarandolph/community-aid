package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.data.models.Like

class LikeRepository : Repository<Like>(Firebase.firestore.collection("likes")) {
    override fun hydrator(doc: Map<String, Any>): Like {
        val like = Like()
        like.complaintId = doc["complaintId"] as String
        like.id = doc["userId"] as String
        return like
    }
}