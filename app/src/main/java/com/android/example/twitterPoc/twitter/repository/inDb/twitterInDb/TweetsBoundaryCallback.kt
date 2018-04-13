package com.android.example.paging.twitterPoc.twitter.repository.inDb.twitterInDb

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import com.android.example.twitterPoc.twitter.api.TwitterApi
import com.android.example.twitterPoc.twitter.util.createStatusLiveData
import com.android.example.twitterPoc.twitter.vo.TweetList
import com.android.example.twitterPoc.twitter.vo.TwitterTweet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
class TweetsBoundaryCallback(
        private val authorization: String,
        private val query: String,
        private val webservice: TwitterApi,
        private val handleResponse: (String, TweetList?) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int
): PagedList.BoundaryCallback<TwitterTweet>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.getTweets(
                    authorization,
                    query,
                    0,
                    0)
                    .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: TwitterTweet) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.getTweets(
                    authorization,
                    query,
                    itemAtEnd.max_id - 1,
                    0)
                    .enqueue(createWebserviceCallback(it))
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: TwitterTweet) {
        // ignored, since we only ever append to what's in the DB
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<TweetList>,
            it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            handleResponse(query, response.body())
            it.recordSuccess()
        }
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<TweetList> {
        return object : Callback<TweetList> {
            override fun onFailure(
                    call: Call<TweetList>,
                    t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                    call: Call<TweetList>,
                    response: Response<TweetList>) {
                response.body()?.apply {
                    val searchMetadata = this.search_metadata
                    var maxId = 0L
                    searchMetadata!!.let {
                        if (searchMetadata.next_results != null) {
                            val refined = searchMetadata.next_results.substring(1, searchMetadata.next_results!!.length)
                            maxId = refined.split("&")[0].split("=")[1].toLong()
                            this.statuses!!.forEach {
                                it.max_id = maxId
                                it.since_id = searchMetadata.since_id
                            }
                        } else {
                            return@let
                        }
                    }

                }
                insertItemsIntoDb(response, it)
            }
        }
    }

}