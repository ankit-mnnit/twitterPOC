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

package com.android.example.twitterPoc.twitter

import android.app.Application
import android.content.Context
import android.support.annotation.VisibleForTesting
import com.android.example.twitterPoc.twitter.api.TwitterApi
import com.android.example.twitterPoc.twitter.db.twitterdb.TwitterDb
import com.android.example.twitterPoc.twitter.repository.TwitterTweetRepository
import com.android.example.paging.twitterPoc.twitter.repository.inDb.twitterInDb.DbTwitterTweetsRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                            app = context.applicationContext as Application,
                            useInMemoryDb = false)
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(type: TwitterTweetRepository.Type, accessToken: String): TwitterTweetRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getTwitterApi(): TwitterApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy {
        TwitterDb.create(app, useInMemoryDb)
    }

    private val api by lazy {
        TwitterApi.create()
    }

    override fun getRepository(type: TwitterTweetRepository.Type, accessToken: String): TwitterTweetRepository {

        return DbTwitterTweetsRepository(
                db = db,
                authorization = "Bearer $accessToken",
                twitterApi = getTwitterApi(),
                ioExecutor = getDiskIOExecutor())

    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getTwitterApi(): TwitterApi = api
}