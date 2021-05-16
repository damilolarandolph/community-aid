package com.prog.communityaid.data.models

import com.prog.communityaid.data.repositories.CommentRepository
import com.prog.communityaid.data.repositories.LikeRepository
import com.prog.communityaid.data.repositories.UserRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class Complaint : Model() {

    lateinit var complaintType: String
    var at: Number = System.currentTimeMillis() / 1000
    lateinit var userId: String
    lateinit var title: String
    lateinit var description: String
    lateinit var picture: String
    lateinit var video: String
    public var solved = false
    var complaintInfo: MutableMap<String, String> = HashMap()
    var lat: Number = 0.0
    var long: Number = 0.0
    private var _comments: List<Comment>? = null
    private var _likes: List<Like>? = null
    private var _user: User? = null
    private var commentRepository = CommentRepository()
    private var likeRepository = LikeRepository()
    private var userRepository = UserRepository()


    override fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["complaintType"] = this.complaintType
        map["at"] = this.at
        map["title"] = this.title
        map["description"] = this.description
        map["video"] = this.video
        map["picture"] = this.picture
        map["lat"] = this.lat
        map["long"] = this.long
        map["userId"] = this.userId
        map["complaintInfo"] = complaintInfo
        map["solved"] = this.solved
        map["id"] = this.id
        return map
    }

    fun getUserAsync(): Deferred<User> {
        return GlobalScope.async {
            if (this@Complaint._user == null) {
                this@Complaint._user = userRepository.findByIdAsync(userId).await()
            }
            return@async this@Complaint._user!!
        }

    }

    fun getCommentsAsync(): Deferred<List<Comment>> {
        return GlobalScope.async {
            if (this@Complaint._comments == null) {
                val query = commentRepository.collection.whereEqualTo("complaintId", id)
                this@Complaint._comments = commentRepository.findWhereAsync(query).await()
            }
            return@async _comments!!
        }
    }

    fun getLikesAsync(): Deferred<List<Like>> {
        return GlobalScope.async {
            if (_likes == null) {
                val query = likeRepository.collection.whereEqualTo("complaintId", id)
                _likes = likeRepository.findWhereAsync(query).await()
            }
            return@async _likes!!
        }
    }


}