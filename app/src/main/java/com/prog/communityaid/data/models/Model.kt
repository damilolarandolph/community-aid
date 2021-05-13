package com.prog.communityaid.data.models

import java.util.*

abstract class Model {

    var id: String = UUID.randomUUID().toString()

    abstract fun toMap(): Map<String, Any>

}