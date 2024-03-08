package com.redwater.appmonitor.data.repository

import com.redwater.appmonitor.data.dao.QuotesDao
import com.redwater.appmonitor.data.firebase.FirebaseFirestoreManager
import com.redwater.appmonitor.data.model.Quote
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class QuotesRepository(private val quotesDao: QuotesDao) {

    private val TAG = this::class.simpleName

    suspend fun insertQuote(quote: Quote){
        Logger.d(TAG, "Inserting record: $quote")
        withContext(Dispatchers.IO){
            quotesDao.insert(quote)
        }
    }

    suspend fun insertQuote(quotes: List<Quote>){
        Logger.d(TAG, "Inserting record: $quotes")
        withContext(Dispatchers.IO){
            quotesDao.insert(quotes)
        }
    }

    suspend fun markAsUsed(id:Int){
        Logger.d(TAG, "marking quote id:$id  as used")
        withContext(Dispatchers.IO){
            quotesDao.markAsUsed(id)
        }
    }

    suspend fun getLastEntryID(): Int{
        Logger.d(TAG, "getting Last Entry ID")
        return withContext(Dispatchers.IO){
            val lastEntryId = quotesDao.getLastEntryID()
            Logger.d(TAG,"Last entry ID  is $lastEntryId")
            lastEntryId
        }
    }

    suspend fun getUsagePercentage(): Int{
        return withContext(Dispatchers.IO){
            val usagePercentage = quotesDao.getUsagePercentage()
            Logger.d(TAG, "usagePercentage for quotes: $usagePercentage")
            usagePercentage
        }
    }

    suspend fun getRandomQuote(): Flow<Quote?> = flow{
            val savedQuote = getRandomQuoteFromDB()
            if (savedQuote != null){
                emit(savedQuote)
            }else{
                emit(null)
                val quote = fetchQuotesFromFirebase()
                emit(quote)
                quote?.let {
                    quotesDao.insert(it)
                }
            }
    }

    private suspend fun getRandomQuoteFromDB(): Quote?{
        return withContext(Dispatchers.IO){
            return@withContext quotesDao.getRandomQuote()
        }
    }

    private suspend fun fetchQuotesFromFirebase(): Quote? {
        return withContext(Dispatchers.IO){
            val firebaseFirestoreManager = FirebaseFirestoreManager()
            val quotes = firebaseFirestoreManager.getNextQuotes(0, 50)
            quotes?.let {
                if (it.isNotEmpty()){
                    return@withContext it.get(0)
                }else{
                    return@withContext null
                }
            }
        }
    }

}