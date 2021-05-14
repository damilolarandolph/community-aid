package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.data.models.Complaint
import java.net.URI

class ComplaintRepository : Repository<Complaint>(Firebase.firestore.collection("complaints")) {
    override fun hydrator(doc: Map<String, Any>): Complaint {
        val complaint = Complaint()
        complaint.id = doc["id"] as String
        complaint.at = doc["at"] as Number
        complaint.description = doc["description"] as String
        complaint.title = doc["title"] as String
        complaint.pictures =
            (doc["pictures"] as List<*>).filterIsInstance<String>().map { item -> URI(item) }
        complaint.videos =
            (doc["videos"] as List<*>).filterIsInstance<String>().map { item -> URI(item) }
        complaint.lat = doc["lat"] as Number
        complaint.long = doc["long"] as Number
        complaint.userId = doc["userId"] as String
        return complaint
    }
}