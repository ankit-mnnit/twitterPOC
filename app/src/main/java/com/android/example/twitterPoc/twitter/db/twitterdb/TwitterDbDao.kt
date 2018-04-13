package com.android.example.twitterPoc.twitter.db.twitterdb

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.android.example.twitterPoc.twitter.vo.TwitterTweet


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
@Dao
interface TwitterDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tweets: List<TwitterTweet>)

    @Query("SELECT * FROM tweets WHERE search_text = :search_text ORDER BY indexInResponse ASC")
    fun tweetsBySearchText(search_text: String): DataSource.Factory<Int, TwitterTweet>

    @Query("SELECT MAX(indexInResponse) + 1 FROM tweets WHERE search_text = :search_text")
    fun getNextIndexInTweetsSearched(search_text: String): Int

    @Query("DELETE FROM tweets WHERE search_text = :search_text")
    fun deleteBySubreddit(search_text: String)
}