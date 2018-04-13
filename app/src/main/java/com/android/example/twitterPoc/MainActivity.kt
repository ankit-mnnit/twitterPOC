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

package com.android.example.twitterPoc

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import com.android.example.twitterPoc.twitter.ServiceLocator
import com.android.example.twitterPoc.twitter.repository.ApiConstants
import com.android.example.twitterPoc.twitter.repository.TwitterTweetRepository
import com.android.example.twitterPoc.twitter.ui.twitter.TwitterActivity
import com.android.example.twitterPoc.twitter.util.PreferenceHelper
import com.android.example.twitterPoc.twitter.vo.TwitterTokenType
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * chooser activity for the demo.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferenceHelper = PreferenceHelper
        val sharedPreferences = preferenceHelper.customPrefs(this, "twitterpoc")
        val authToken = sharedPreferences.getString("authToken", null)

        when(authToken) {

            null -> {
                withDatabase.setOnClickListener {
                    show(TwitterTweetRepository.Type.DB)
                }
            }
            else -> startTwitterActivity()
        }
    }

    private fun show(type: TwitterTweetRepository.Type) {

        getAuthToken()


    }

    private fun getAuthToken() {

        ServiceLocator.instance(this)
                .getTwitterApi()
                .getAuthToken(
                        "Basic ${getBase64(ApiConstants.BEARER_TOKEN_CREDENTIALS)}",
                        "client_credentials")
                .enqueue(object: Callback<TwitterTokenType> {
                    /**
                     * Invoked when a network exception occurred talking to the server or when an unexpected
                     * exception occurred creating the request or processing the response.
                     */
                    override fun onFailure(call: Call<TwitterTokenType>?, t: Throwable?) {
                        Log.e("Twitter", "Login Failure ${t!!.message}")
                    }

                    /**
                     * Invoked for a received HTTP response.
                     *
                     *
                     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                     * Call [Response.isSuccessful] to determine if the response indicates success.
                     */
                    override fun onResponse(call: Call<TwitterTokenType>?, response: Response<TwitterTokenType>?) {
                        Log.d("Twitter", "Login success ${response!!.body()!!.access_token}")
                        val preferenceHelper = PreferenceHelper
                        val sharedPreferences = preferenceHelper.customPrefs(this@MainActivity, "twitterpoc")
                        sharedPreferences.edit().putString("authToken", response.body()!!.access_token).apply()
                        startTwitterActivity()
                    }
                })

    }

    private fun startTwitterActivity() {

        val intent = Intent(this@MainActivity, TwitterActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun getBase64(value: String): String {
        return Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP).toString()
    }
}
