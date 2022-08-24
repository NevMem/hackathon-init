package com.nevmem.survey.push

import com.google.firebase.messaging.FirebaseMessagingService

class EthnosurveyPushService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("cur_deb $token")
    }
}
