package com.prog.communityaid.data.models

class Like : Model() {

    lateinit var complaintId: String

    override fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["complaintId"] = this.complaintId
        return map
    }


}