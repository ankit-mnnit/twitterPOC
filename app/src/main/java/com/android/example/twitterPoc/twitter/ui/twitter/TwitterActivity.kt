package com.android.example.twitterPoc.twitter.ui.twitter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.android.example.twitterPoc.twitter.util.PreferenceHelper
import com.android.example.twitterPoc.R
import com.android.example.twitterPoc.twitter.ServiceLocator
import com.android.example.twitterPoc.twitter.repository.NetworkState
import com.android.example.twitterPoc.twitter.repository.TwitterTweetRepository
import com.android.example.twitterPoc.twitter.ui.PostsAdapter
import com.android.example.twitterPoc.twitter.vo.TwitterTweet
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_twitter.*


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
class TwitterActivity: AppCompatActivity() {

    companion object {
        const val DEFAULT_SEARCH = "ipl2018"
        const val KEY_SEARCH_TEXT = "search_text"
    }

    private lateinit var model: TwitterTweetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter)
        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()
        val searchText = savedInstanceState?.getString(KEY_SEARCH_TEXT) ?: DEFAULT_SEARCH
        model.showTweets(searchText)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, model.currentSearchedTweets())
    }

    private fun initSearch() {
        input.setOnEditorActionListener({ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateTweetsFromQuery()
                true
            } else {
                false
            }
        })
        input.setOnKeyListener({ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateTweetsFromQuery()
                true
            } else {
                false
            }
        })

    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }

    }

    private fun initAdapter() {
        val glide = Glide.with(this)
        val adapter = PostsAdapter(glide) {
            model.retry()
        }
        list.adapter = adapter
        model.posts.observe(this, Observer<PagedList<TwitterTweet>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })

    }

    private fun getViewModel(): TwitterTweetViewModel {
        val preferenceHelper = PreferenceHelper
        val sharedPreferences = preferenceHelper.customPrefs(this, "twitterpoc")
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repoType = TwitterTweetRepository.Type.values()[2]
                val repo = ServiceLocator.instance(this@TwitterActivity)
                        .getRepository(repoType, sharedPreferences.getString("authToken", null))
                @Suppress("UNCHECKED_CAST")
                return TwitterTweetViewModel(repo) as T
            }
        })[TwitterTweetViewModel::class.java]
    }

    private fun updateTweetsFromQuery() {
        input.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (model.showTweets(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? PostsAdapter)?.submitList(null)
                }
            }
        }
    }

}