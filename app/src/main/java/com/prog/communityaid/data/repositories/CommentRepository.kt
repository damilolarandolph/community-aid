package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.data.models.Comment

class CommentRepository : Repository<Comment>(Firebase.firestore.collection("comments")) {
    override fun hydrator(doc: Map<String, Any>): Comment {
        val comment = Comment()
        comment.body = doc["body"] as String
        comment.complaintId = doc["complaintId"] as String
        comment.userId = doc["userId"] as String
        comment.id = doc["id"] as String
        return comment
    }
}