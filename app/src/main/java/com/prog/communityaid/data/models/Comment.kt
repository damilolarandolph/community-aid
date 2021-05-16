package com.prog.communityaid.data.models

import com.prog.communityaid.data.repositories.UserRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class Comment : Model() {

    lateinit var userId: String
    lateinit var complaintId: String
    lateinit var body: String
    private var userRepository = UserRepository()
    private var _user: User? = null


    fun getUser(): Deferred<User> {
        return GlobalScope.async {
            if (_user == null) {
                _user = userRepository.findByIdAsync(userId).await()
            }

            return@async _user!!
        }
    }

    override fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["userId"] = this.userId
        map["complaintId"] = this.complaintId
        map["body"] = this.body
        map["id"] = this.id
        return map
    }
}