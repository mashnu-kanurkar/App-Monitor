package com.redwater.appmonitor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.Quote

@Dao
interface QuotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quotesList: List<Quote>)

    @Query("SELECT MAX(${Constants.QuotesColumns.id}) FROM ${Constants.quoteTable}")
    fun getLastEntryID(): Int

    @Query("SELECT * FROM ${Constants.quoteTable} ORDER BY RANDOM() LIMIT 1")
    fun getRandomQuote(): Quote?

    @Query("UPDATE ${Constants.quoteTable} SET ${Constants.QuotesColumns.isUsed} = 1 WHERE ${Constants.QuotesColumns.id} =:id")
    fun markAsUsed(id: Int)

    @Query("SELECT CASE COUNT(*) WHEN 0 THEN 71 ELSE COUNT(*)*100/( SELECT COUNT(*) FROM ${Constants.quoteTable}) END FROM ${Constants.quoteTable} WHERE ${Constants.QuotesColumns.isUsed} =1")
    fun getUsagePercentage(): Int
}