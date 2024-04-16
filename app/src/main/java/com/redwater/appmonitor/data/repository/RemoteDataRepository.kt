package com.redwater.appmonitor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDataRepository {
    suspend fun getQuote(){
        return withContext(Dispatchers.IO){

        }
    }
}