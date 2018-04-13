package com.android.example.twitterPoc.twitter.db.twitterdb

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.android.example.twitterPoc.twitter.vo.TwitterTweet
import com.android.example.twitterPoc.twitter.vo.UserConverter


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
@Database(
        entities = arrayOf(TwitterTweet::class),
        version = 1,
        exportSchema = false
)
@TypeConverters(value = [(UserConverter::class)])
abstract class TwitterDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory : Boolean): TwitterDb {
            val databaseBuilder = if(useInMemory) {
                Room.inMemoryDatabaseBuilder(context, TwitterDb::class.java)
            } else {
                Room.databaseBuilder(context, TwitterDb::class.java, "tweets.db")
            }
            return databaseBuilder
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun tweets(): TwitterDbDao
}