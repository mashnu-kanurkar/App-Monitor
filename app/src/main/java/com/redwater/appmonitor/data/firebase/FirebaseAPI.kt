package com.redwater.appmonitor.data.firebase

import com.google.firebase.firestore.Query
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

 @OptIn(ExperimentalCoroutinesApi::class)
    suspend inline fun <reified T> Query.execute(): List<T>?{
        Logger.d("execute", "querying data: $this")
        return try {
            suspendCancellableCoroutine { cont->
                this.get()
                    .apply {
                        addOnCompleteListener {
                            try {
                                Logger.d("execute", "firestore query completed")
                                if (isSuccessful){
                                    val snap = result
                                    val matchingDocs: MutableList<T> = ArrayList()
                                    for (doc in snap.documents){
                                        Logger.d("execute", "doc: ${doc.data}")
                                        val payload = doc.toObject(T::class.java)
                                        Logger.d("execute", "payload: $payload")
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
            Logger.d("execute", "Exception while getNextPuzzleOverlayData : ${e.message}")
            e.printStackTrace()
            null
        }
    }