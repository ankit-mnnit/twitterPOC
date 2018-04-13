package com.android.example.paging.twitterPoc.twitter.repository.inDb.twitterInDb

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.android.example.twitterPoc.twitter.api.TwitterApi
import com.android.example.twitterPoc.twitter.db.twitterdb.TwitterDb
import com.android.example.twitterPoc.twitter.repository.Listing
import com.android.example.twitterPoc.twitter.repository.NetworkState
import com.android.example.twitterPoc.twitter.repository.TwitterTweetRepository
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
class DbTwitterTweetsRepository(
        val db: TwitterDb,
        private val authorization: String,
        private val twitterApi: TwitterApi,
        private val ioExecutor: Executor,
        private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE
): TwitterTweetRepository {

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(search_text: String, body: TweetList?) {
        var maxId = 0L
        body!!.search_metadata.apply {
            val searchMetadata = this
            searchMetadata!!.let {
                if (searchMetadata.next_results != null) {
                    val refined = searchMetadata.next_results.substring(1, searchMetadata.next_results.length)
                    maxId = refined.split("&")[0].split("=")[1].toLong()
                    body.statuses.let { posts ->
                        db.runInTransaction {

                            val start = db.tweets().getNextIndexInTweetsSearched(search_text)
                            val items = posts!!.mapIndexed { index, child ->
                                child.indexInResponse = start + index
                                child.searchText = search_text
                                child.max_id = maxId
                                child.since_id = body.search_metadata!!.since_id
                                child
                            }
                            db.tweets().insert(items)
                        }
                    }
                } else {
                    return
                }
            }
        }

    }

    override fun tweetsOfQuery(query: String, pageSize: Int): Listing<TwitterTweet> {

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = TweetsBoundaryCallback(
                authorization = authorization,
                webservice = twitterApi,
                query = query,
                handleResponse = this::insertResultIntoDb,
                ioExecutor = ioExecutor,
                networkPageSize = networkPageSize)
        // create a data source factory from Room
        val dataSourceFactory = db.tweets().tweetsBySearchText(query)
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize)
                .setBoundaryCallback(boundaryCallback)

        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            refresh(query)
        })

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.helper.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(search_text: String): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        twitterApi.getTweets(authorization, search_text, 0, 0).enqueue(
                object : Callback<TweetList> {
                    override fun onFailure(call: Call<TweetList>, t: Throwable) {
                        // retrofit calls this on main thread so safe to call set value
                        networkState.value = NetworkState.error(t.message)
                    }

                    override fun onResponse(
                            call: Call<TweetList>,
                            response: Response<TweetList>) {
                        ioExecutor.execute {

                            response.body()?.apply {
                                val searchMetadata = this.search_metadata
                                var maxId = 0L
                                searchMetadata!!.let {
                                    if (searchMetadata.next_results != null) {
                                        val refined = searchMetadata.next_results.substring(1, searchMetadata.next_results.length)
                                        maxId = refined.split("&")[0].split("=")[1].toLong()
                                        this.statuses!!.forEach {
                                            it.max_id = maxId
                                            it.since_id = searchMetadata.since_id
                                        }
                                        db.runInTransaction {
                                            db.tweets().deleteBySubreddit(search_text)
                                            insertResultIntoDb(search_text, response.body())
                                        }
                                    } else {
                                        networkState.postValue(NetworkState.LOADED)
                                        return@execute
                                    }
                                }

                            }


                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                }
        )
        return networkState
    }


    companion object {
        private val DEFAULT_NETWORK_PAGE_SIZE = 20
    }

}