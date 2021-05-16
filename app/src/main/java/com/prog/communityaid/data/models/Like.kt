package com.prog.communityaid.data.models

class Like : Model() {

    lateinit var complaintId: String
    lateinit var userId: String

    override fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["complaintId"] = this.complaintId
        map["id"] = this.id
        map["userId"] = this.userId
        return map
    }


}