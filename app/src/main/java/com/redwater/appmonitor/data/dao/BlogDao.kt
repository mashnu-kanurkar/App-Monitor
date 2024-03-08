package com.redwater.appmonitor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.model.Blog

@Dao
interface BlogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blog: Blog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogList: List<Blog>)

    @Query("SELECT MAX(${Constants.BlogColumns.id}) FROM ${Constants.blogTable}")
    fun getLastEntryID(): Int

    @Query("SELECT * FROM ${Constants.blogTable} ORDER BY RANDOM() LIMIT 1")
    fun getRandomBlog(): Blog?

    @Query("UPDATE ${Constants.blogTable} SET ${Constants.BlogColumns.isUsed} = 1 WHERE ${Constants.BlogColumns.id} =:id")
    fun markAsUsed(id: Int)

    @Query("SELECT CASE COUNT(*) WHEN 0 THEN 71 ELSE COUNT(*)*100/( SELECT COUNT(*) FROM ${Constants.blogTable}) END FROM ${Constants.blogTable} WHERE ${Constants.BlogColumns.isUsed} =1")
    fun getUsagePercentage(): Int
}