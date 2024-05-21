package com.redwater.appmonitor.data.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.redwater.appmonitor.data.model.Blog
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.data.model.Quote

class FirebaseFirestoreManager {
    private val firebaseDb = Firebase.firestore
    suspend fun getNextQuotes(lastFetchedId: Int, limit: Int): List<Quote>? {
        return firebaseDb.collection(FirebaseConstants.quotesCollection)
            .whereGreaterThan("id", lastFetchedId)
            .whereLessThan("id", lastFetchedId+limit)
            .execute<Quote>()
    }

    suspend fun getNextBlog(lastFetchedId: Int, limit: Int): List<Blog>?{
        return firebaseDb.collection(FirebaseConstants.blogCollection)
            .whereGreaterThan("id", lastFetchedId)
            .whereLessThan("id", lastFetchedId + limit)
            .execute<Blog>()
    }
}
