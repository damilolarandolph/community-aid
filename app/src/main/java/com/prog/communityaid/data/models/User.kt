package com.prog.communityaid.data.models

class User : Model() {

    public lateinit var name: String
    public lateinit var email: String


    override fun toMap(): Map<String, Any> {
        var map = HashMap<String, Any>()
        map["name"] = this.name
        map["email"] = this.email
        map["id"] = this.id
        return map
    }
}