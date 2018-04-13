package com.android.example.twitterPoc.twitter.repository


/**
 * Created by Ankit Garg on 12/04/18.
 * Company <Reliance Payment Solutions Ltd.>
 * Email <ankit1.garg@ril.com>
 */
class ApiConstants {

    companion object {


        val CONSUMER_KEY = "nPLmvW1LRFc23ud9IVddqYiR0"

        val CONSUMER_SECRET = "Bzc9HYrCwiyToJfFD4TYAoFMBSn4lcIbT0eSoFQi0ahPfdMM7l"

        val TWITTER_SEARCH_URL = "https://api.twitter.com"

        val BEARER_TOKEN_CREDENTIALS = "${CONSUMER_KEY}:${CONSUMER_SECRET}"

        const val TWITTER_HASHTAG_SEARCH_CODE = "/1.1/search/tweets.json"
    }

}