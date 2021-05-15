package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prog.communityaid.data.models.Complaint

class ComplaintRepository : Repository<Complaint>(Firebase.firestore.collection("complaints")) {
    override fun hydrator(doc: Map<String, Any>): Complaint {
        val complaint = Complaint()
        complaint.id = doc["id"] as String
        complaint.at = doc["at"] as Number
        complaint.description = doc["description"] as String
        complaint.title = doc["title"] as String
        complaint.video = doc["video"] as String
        complaint.picture = doc["picture"] as String
        complaint.lat = doc["lat"] as Number
        complaint.long = doc["long"] as Number
        complaint.userId = doc["userId"] as String
        @Suppress("UNCHECKED_CAST")
        complaint.complaintInfo = (doc["complaintInfo"] as MutableMap<String, String>)
        return complaint
    }
}