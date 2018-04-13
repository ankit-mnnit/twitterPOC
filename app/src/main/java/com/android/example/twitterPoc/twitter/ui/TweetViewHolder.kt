/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.twitterPoc.twitter.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.example.twitterPoc.R
import com.android.example.twitterPoc.twitter.vo.TwitterTweet
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

/**
 * A RecyclerView ViewHolder that displays a reddit post.
 */
class TweetViewHolder(view: View, private val glide: RequestManager)
    : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private val thumbnail : ImageView = view.findViewById(R.id.thumbnail)
    private var post : TwitterTweet? = null
    init {
        view.setOnClickListener {
            post!!.user.profile_image_url.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(tweet: TwitterTweet?) {
        this.post = tweet
        title.text = tweet?.text ?: "loading"
        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
                tweet?.source ?: "unknown")
        score.text = "${tweet?.favorite_count ?: 0}"
        if (tweet!!.user.profile_image_url.startsWith("http") == true) {
            thumbnail.visibility = View.VISIBLE
            glide.load(tweet.user.profile_image_url).centerCrop()
                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
                    .into(thumbnail)
        } else {
            thumbnail.visibility = View.GONE
            Glide.clear(thumbnail)
        }

    }

    companion object {
        fun create(parent: ViewGroup, glide: RequestManager): TweetViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tweet, parent, false)
            return TweetViewHolder(view, glide)
        }
    }

    fun updateFavourites(item: TwitterTweet?) {
        post = item
        score.text = "${item?.favorite_count ?: 0}"
    }
}