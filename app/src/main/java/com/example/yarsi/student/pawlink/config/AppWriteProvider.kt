package com.example.yarsi.student.pawlink.config

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime

object AppWriteProvider {

    private lateinit var client: Client

    lateinit var account: Account
        private set

    lateinit var databases: Databases
        private set

    lateinit var storage: Storage
        private set

    lateinit var realtime: Realtime
        private set

    private const val API_ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    private const val PROJECT_ID = "6a152cbc0019ae4592b6"

    fun init(context: Context) {

        client = Client(context.applicationContext)
            .setEndpoint(API_ENDPOINT)
            .setProject(PROJECT_ID)

        account = Account(client)

        databases = Databases(client)

        storage = Storage(client)

        realtime = Realtime(client)
    }
}