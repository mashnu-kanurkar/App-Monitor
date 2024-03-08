package com.redwater.appmonitor.data.repository

import com.redwater.appmonitor.data.dao.BlogDao
import com.redwater.appmonitor.data.firebase.FirebaseFirestoreManager
import com.redwater.appmonitor.data.model.Blog
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class BlogRepository(private val blogDao: BlogDao) {

    private val TAG = this::class.simpleName
    suspend fun insertBlog(blog: Blog){
        Logger.d(TAG, "Inserting record: $blog")
        withContext(Dispatchers.IO){
            blogDao.insert(blog)
        }
    }

    suspend fun insertBlog(blogs: List<Blog>){
        Logger.d(TAG, "Inserting record: $blogs")
        withContext(Dispatchers.IO){
            blogDao.insert(blogs)
        }
    }

    suspend fun getLastEntryID(): Int{
        Logger.d(TAG, "getting Last Entry ID")
        return withContext(Dispatchers.IO){
            val lastEntryId = blogDao.getLastEntryID()
            Logger.d(TAG,"Last entry ID  is $lastEntryId")
            lastEntryId
        }
    }

    suspend fun markAsUsed(id: Int){
        Logger.d(TAG, "marking blog id:$id  as used")
        withContext(Dispatchers.IO){
            blogDao.markAsUsed(id)
        }
    }

    suspend fun getUsagePercentage(): Int{
        return withContext(Dispatchers.IO){
            val usagePercentage = blogDao.getUsagePercentage()
            Logger.d(TAG, "usagePercentage for quotes: $usagePercentage")
            usagePercentage
        }
    }

    suspend fun getRandomBlog(): Flow<Blog?> = flow{
            val savedBlog = getRandomBlogFromDB()
            if (savedBlog != null){
                emit(savedBlog)
            }else{
                emit(null)
                val blog = fetchBlogFromFirebase()
                emit(blog)
                blog?.let {
                    blogDao.insert(it)
                }
            }
    }

    private suspend fun getRandomBlogFromDB(): Blog?{
        return withContext(Dispatchers.IO){
            return@withContext blogDao.getRandomBlog()
        }
    }

    private suspend fun fetchBlogFromFirebase(): Blog? {
        return withContext(Dispatchers.IO){
            val firebaseFirestoreManager = FirebaseFirestoreManager()
            val blogs = firebaseFirestoreManager.getNextBlog(0, 50)
            blogs?.let {
                if (it.isNotEmpty()){
                    return@withContext it.get(0)
                }else{
                    return@withContext null
                }
            }
        }
    }
}