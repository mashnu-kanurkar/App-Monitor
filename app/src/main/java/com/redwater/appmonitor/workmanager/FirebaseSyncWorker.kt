package com.redwater.appmonitor.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.firebase.FirebaseFirestoreManager

class FirebaseSyncWorker(private val appContext: Context,
                         private val workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams ) {
    override suspend fun doWork(): Result {
        return try {
            //val overlayDataRepository = (appContext as AppMonitorApp).overlayDataRepository
            val appMonitor = appContext as AppMonitorApp
            val quotesRepository = appMonitor.quotesRepository
            val blogRepository = appMonitor.blogRepository
            val firebaseFirestoreManager = FirebaseFirestoreManager()
            var isAnySuccess = false

            try {
                val quotesUsagePercentage = quotesRepository.getUsagePercentage()
                if (quotesUsagePercentage > 70){
                    val lastQuotesID = quotesRepository.getLastEntryID()
                    val quoteList = firebaseFirestoreManager.getNextQuotes(lastFetchedId = lastQuotesID, limit = 50)
                    quoteList?.let {
                        quotesRepository.insertQuote(it)
                    }
                }
                isAnySuccess = true
            }catch (e: Exception){
                e.printStackTrace()
            }

            try {
                val blogUsagePercentage = blogRepository.getUsagePercentage()
                if (blogUsagePercentage > 70){
                    val lastBlogID = blogRepository.getLastEntryID()
                    val blogList = firebaseFirestoreManager.getNextBlog(lastFetchedId = lastBlogID, limit = 50)
                    blogList?.let {
                        blogRepository.insertBlog(it)
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