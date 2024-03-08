package com.redwater.appmonitor.data.repository

import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.dao.OverlayDataDao
import com.redwater.appmonitor.data.firebase.FirebaseConstants
import com.redwater.appmonitor.data.firebase.FirebaseFirestoreManager
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OverlayDataRepository(private val overlayDataDao: OverlayDataDao) {
    private val TAG = this::class.simpleName

    suspend fun markAsUSed(id: Int){
        Logger.d(TAG, "marking overlay data id:$id  as used")
        withContext(Dispatchers.IO){
            overlayDataDao.markAsUsed(id)
        }
    }

    suspend fun insertOverlayPayloadList(overlayPayloadList: List<OverlayPayload>){
        Logger.d(TAG, "insertOverlayPayloadList $overlayPayloadList")
        withContext(Dispatchers.IO){
            overlayDataDao.insert(overlayPayloadList)
        }
    }

    suspend fun insertOverlayPayload(overlayPayload: OverlayPayload){
        Logger.d(TAG, "insertOverlayPayload")
        withContext(Dispatchers.IO){
            overlayDataDao.insert(overlayPayload)
        }
    }

    suspend fun getLastEntryID(type: String): Int{
        Logger.d(TAG, "getLastEntryID for $type")
        return withContext(Dispatchers.IO){
            val lastEnrtyId = overlayDataDao.getLastEntryID(type = type)
            Logger.d(TAG,"Last enrty ID for $type is $lastEnrtyId")
            lastEnrtyId
        }
    }

    suspend fun getRandomOverlayPayload(): OverlayPayload? {
        Logger.d(TAG, "getRandomOverlayPayload")
        return withContext(Dispatchers.IO){
            val overlayData = overlayDataDao.getGreaterDifficultyOverlayData(0, 100)
            if (overlayData == null){
                val firebaseFirestoreManager = FirebaseFirestoreManager()
                return@withContext null
            }else{
                return@withContext overlayData
            }
        }
    }

    suspend fun getRandomQuote(): String{
        Logger.d(TAG, "getRandomQuote")
        return withContext(Dispatchers.IO){
            val quotes = overlayDataDao.getRandomEntry(type = FirebaseConstants.quotesType)
            if (quotes == null){

                return@withContext Constants.defQuote
            }else{
                return@withContext quotes.data
            }
        }
    }

    suspend fun getUsagePercentageOf(type: String): Int{
        Logger.d(TAG, "getUsagePercentageOf $type")
        return withContext(Dispatchers.IO){
            val usagePercentage = overlayDataDao.getUsagePercentageOf(type = type)
            Logger.d(TAG, "usagePercentage for $type: $usagePercentage")
            usagePercentage
        }
    }



}