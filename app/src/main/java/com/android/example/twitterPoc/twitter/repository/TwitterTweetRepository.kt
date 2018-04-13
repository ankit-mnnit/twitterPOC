package com.android.example.twitterPoc.twitter.repository

import com.android.example.twitterPoc.twitter.vo.TwitterTweet


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
interface TwitterTweetRepository {
    fun tweetsOfQuery(query: String, pageSize: Int): Listing<TwitterTweet>

    enum class Type {
        IN_MEMORY_BY_ITEM,
        IN_MEMORY_BY_PAGE,
        DB
    }
}