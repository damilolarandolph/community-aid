package com.prog.communityaid.storage

import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.io.File

class StorageController {

    companion object {
        private var storageRef = Firebase.storage.reference

        fun upload(file: File): Deferred<String> {
            return GlobalScope.async {
                val fileRef = storageRef.child(file.name)
                var result = fileRef.putFile(file.toUri()).await()
                return@async file.name
            }
        }

        fun getFile(fileName: String, file: File): Deferred<File> {
            return GlobalScope.async {
                val fileRef = storageRef.child(fileName)
                fileRef.getFile(file).await()
                return@async file
            }
        }

    }

}