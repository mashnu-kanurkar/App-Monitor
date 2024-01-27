package com.redwater.appmonitor.data.firebase

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseFirestoreManager {
    private val TAG = this::class.simpleName
    private val firebaseDb = Firebase.firestore

    suspend fun getNextPuzzles(lastFetchedId: Int, limit: Int): List<OverlayPayload>? {
        return firebaseDb.collection(FirebaseConstants.puzzleCollection)
            .whereGreaterThan("id", lastFetchedId)
            .whereLessThan("id", lastFetchedId+limit)
            .getOverlayPayload()
    }

    suspend fun getNextMemes(lastFetchedId: Int, limit: Int): List<OverlayPayload>? {
        return firebaseDb.collection(FirebaseConstants.memesCollection)
            .whereGreaterThan("id", lastFetchedId)
            .whereLessThan("id", lastFetchedId+limit)
            .getOverlayPayload()
    }

    suspend fun getNextQuotes(lastFetchedId: Int, limit: Int): List<OverlayPayload>? {
        return firebaseDb.collection(FirebaseConstants.quotesCollection)
            .whereGreaterThan("id", lastFetchedId)
            .whereLessThan("id", lastFetchedId+limit)
            .getOverlayPayload()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Query.getOverlayPayload(): List<OverlayPayload>?{
        Logger.d(TAG, "querying data: getNextPuzzleOverlayData")
        return try {
            suspendCancellableCoroutine { cont->
                this.get()
                    .apply {
                        addOnCompleteListener {
                            try {
                                Logger.d(TAG, "getNextPuzzleOverlayData query completed")
                                if (isSuccessful){
                                    val snap = result
                                    val matchingDocs: MutableList<OverlayPayload> = ArrayList()
                                    for (doc in snap.documents){
                                        Logger.d(TAG, "doc: ${doc.data}")
                                        val payload = doc.toObject(OverlayPayload::class.java)
                                        Logger.d(TAG, "payload: $payload")
                                        if (payload != null) {
                                            matchingDocs.add(payload)
                                        }
                                    }
                                    if (cont.isActive){
                                        cont.resume(matchingDocs.toList()){}
                                    }

                                }else{
                                    if (cont.isActive){
                                        cont.resume(emptyList()){}
                                    }
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }catch (e: Exception){
            Logger.d(TAG, "Exception while getNextPuzzleOverlayData : ${e.message}")
            e.printStackTrace()
            null
        }
    }
}