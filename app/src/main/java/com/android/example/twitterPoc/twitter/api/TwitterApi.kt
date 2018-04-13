package com.android.example.twitterPoc.twitter.api

import android.util.Log
import com.android.example.twitterPoc.twitter.repository.ApiConstants
import com.android.example.twitterPoc.twitter.vo.TweetList
import com.android.example.twitterPoc.twitter.vo.TwitterTokenType
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
interface TwitterApi {
    @GET(ApiConstants.TWITTER_HASHTAG_SEARCH_CODE)
    fun getTweets(@Header("Authorization") authorization: String,
                  @Query("q") hashtag: String,
                  @Query("max_id") maxId: Long = 0,
                  @Query("since_id") sinceId: Long = 0,
                  @Query("count") count: Int? = 20): Call<TweetList>

    @FormUrlEncoded
    @POST("/oauth2/token")
    fun getAuthToken(@Header("Authorization") authorization: String,
                     @Field("grant_type") grantType: String): Call<TwitterTokenType>

    companion object {
        fun create(): TwitterApi = create(HttpUrl.parse(ApiConstants.TWITTER_SEARCH_URL)!!)
        fun create(httpUrl: HttpUrl): TwitterApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TwitterApi::class.java)
        }
    }
}