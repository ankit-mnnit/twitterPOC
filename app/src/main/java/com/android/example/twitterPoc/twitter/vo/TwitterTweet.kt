package com.android.example.twitterPoc.twitter.vo

import android.arch.persistence.room.*
import android.content.ContentValues
import android.provider.BaseColumns
import android.support.annotation.NonNull
import com.google.gson.Gson
import java.util.*
import kotlin.math.roundToLong


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
@Entity(tableName = "tweets",
        indices = arrayOf(Index(value = "search_text", unique = false)))
data class TwitterTweet(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        var id: Long,
        @ColumnInfo(name = "search_text")
        var searchText: String,
        var text: String,
        var favorite_count: Long,
        @NonNull
        @ColumnInfo(name = "created_at")
        var created_at: String,
        var max_id: Long,
        var since_id: Long,
        var source: String,
        @TypeConverters(UserConverter::class)
        var user: User
) {
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1

    companion object {

        fun fromContentValues(values: ContentValues): TwitterTweet {

            val tweetModel = TwitterTweet(
                    getRandomLong(),
                    getRandomString(),
                    getRandomString(),
                    getRandomLong(),
                    getRandomString(),
                    getRandomLong(),
                    getRandomLong(),
                    getRandomString(),
                    getRandomUser())

            tweetModel.apply {
                tweetModel.id = values.getAsLong(BaseColumns._ID)
                tweetModel.searchText = values.getAsString("search_text")
                tweetModel.text = values.getAsString("text")
                tweetModel.favorite_count = values.getAsLong("favorite_count")
                tweetModel.created_at = values.getAsString("created_at")
                tweetModel.max_id = values.getAsLong("max_id")
                tweetModel.since_id = values.getAsLong("since_id")
                tweetModel.source = values.getAsString("source")
                tweetModel.user = Gson().fromJson(values.getAsString("user"), User::class.java)
            }

            return tweetModel
        }

        fun toContentValues(tweet: TwitterTweet): ContentValues {
            val contentValue = ContentValues()
            return contentValue.apply {
                put(BaseColumns._ID, tweet.id)
                put("search_text", tweet.searchText)
                put("text", tweet.text)
                put("favorite_count", tweet.favorite_count)
                put("created_at", tweet.created_at)
                put("max_id", tweet.max_id)
                put("since_id", tweet.since_id)
                put("source", tweet.source)
                put("user", Gson().toJson(tweet.user))
            }

        }

        private fun getRandomString(): String {
            return java.util.UUID.randomUUID().toString()
        }

        private fun getRandomUser(): User {
            return User(getRandomString())

        }

        private fun getRandomLong(): Long {
            val range = 1234567L
            val r = Random()
            return (r.nextDouble() * range).roundToLong()
        }
    }
}

class TweetList(status: List<TwitterTweet>?, searchMetadata: TwitterSearchMetadata?) {
    var statuses: List<TwitterTweet>? = status
    var search_metadata: TwitterSearchMetadata? = searchMetadata
}

data class TwitterSearchMetadata(var max_id: Long, var since_id: Long, var next_results: String)

data class User(val profile_image_url: String)

data class TwitterTokenType(val access_token: String, val token_type: String)

class UserConverter {

    @TypeConverter
    fun convertStringToUser(imageUrl: String) : User {
        return User(imageUrl)
    }

    @TypeConverter
    fun convertUserToString(user: User) : String {
        return user.profile_image_url
    }
}