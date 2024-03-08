package com.redwater.appmonitor.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.firebase.FirebaseConstants
import com.redwater.appmonitor.data.firebase.FirebaseFirestoreManager

class FirebaseSyncWorker(private val appContext: Context,
                         private val workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams ) {
    override suspend fun doWork(): Result {
        return try {
            val overlayDataRepository = (appContext as AppMonitorApp).overlayDataRepository
            val firebaseFirestoreManager = FirebaseFirestoreManager()
            var isAnySuccess = false
            try {
                val puzzleUsagePercentage = overlayDataRepository.getUsagePercentageOf(FirebaseConstants.puzzleType)
                if (puzzleUsagePercentage > 70){
                    val lastPuzzleID = overlayDataRepository.getLastEntryID(FirebaseConstants.puzzleType)
                    val puzzleList = firebaseFirestoreManager.getNextPuzzles(lastFetchedId = lastPuzzleID, limit = 50)
                    puzzleList?.let {
                        overlayDataRepository.insertOverlayPayloadList(it)
                    }
                }
                isAnySuccess = true
            }catch (e: Exception){
                e.printStackTrace()
            }

            try {
                val memeUsagePercentage = overlayDataRepository.getUsagePercentageOf(FirebaseConstants.memesType)
                if (memeUsagePercentage > 70){
                    val lastMemesID = overlayDataRepository.getLastEntryID(FirebaseConstants.memesType)
                    val memeList = firebaseFirestoreManager.getNextMemes(lastFetchedId = lastMemesID, limit = 50)
                    memeList?.let {
                        overlayDataRepository.insertOverlayPayloadList(it)
                    }
                }
                isAnySuccess = true
            }catch (e: Exception){
                e.printStackTrace()
            }

            try {
                val quotesUsagePercentage = overlayDataRepository.getUsagePercentageOf(FirebaseConstants.quotesType)
                if (quotesUsagePercentage > 70){
                    val lastQuotesID = overlayDataRepository.getLastEntryID(FirebaseConstants.quotesType)
                    val quoteList = firebaseFirestoreManager.getNextQuotes(lastFetchedId = lastQuotesID, limit = 50)
                    quoteList?.let {
                        overlayDataRepository.insertOverlayPayloadList(it)
                    }
                }
                isAnySuccess = true
            }catch (e: Exception){
                e.printStackTrace()
            }
            if (isAnySuccess.not()){
                Result.failure()
            }
            Result.success()
        }catch (e: Exception){
            e.printStackTrace()
            Result.failure()
        }
    }
}