package com.android.example.twitterPoc.twitter.ui.twitter

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.android.example.twitterPoc.twitter.repository.TwitterTweetRepository


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
class TwitterTweetViewModel(private val repository: TwitterTweetRepository): ViewModel() {

    private val query = MutableLiveData<String>()
    private val repoResult = map(query, {
        repository.tweetsOfQuery(it, 30)
    })

    val posts = switchMap(repoResult, { it.pagedList })
    val networkState = switchMap(repoResult, { it.networkState })
    val refreshState = switchMap(repoResult, { it.refreshState })

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showTweets(search: String): Boolean {
        if (query.value == search) {
            return false
        }

        query.value = search
        return true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun currentSearchedTweets(): String? = query.value

}