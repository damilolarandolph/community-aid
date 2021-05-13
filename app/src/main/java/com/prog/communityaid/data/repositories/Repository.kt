package com.prog.communityaid.data.repositories

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.prog.communityaid.data.models.Model
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

abstract class Repository<T : Model>(_collection: CollectionReference) {
    var collection: CollectionReference = _collection
        get() = field
        protected set(value: CollectionReference) {
            field = value
        }

    fun findById(id: String): Deferred<T> {

        return GlobalScope.async {
            var snapShot = this@Repository.collection.document(id).get().await();
            var data = this@Repository.hydrator(snapShot.data as Map<String, Any>)
            return@async data
        }

    }

    fun findAll(): Deferred<List<T>> {
        return GlobalScope.async {
            var snapShot = this@Repository.collection.get().await();
            var data =
                snapShot.documents.map { doc -> this@Repository.hydrator(doc.data as Map<String, Any>) }
            return@async data
        }
    }

    fun save(model: T): Deferred<Unit> {
        return GlobalScope.async {
            var map = model.toMap()
            this@Repository.collection.document(model.id).set(map, SetOptions.merge())
            return@async
        }
    }

    public fun update(model: T) {

    }


    public fun findWhere(query: Query): Deferred<List<T>> {
        return GlobalScope.async {
            var results = query.get().await()
            return@async results.map { result -> hydrator(result.data as Map<String, Any>) }
        }
    }


    abstract fun hydrator(doc: Map<String, Any>): T

}