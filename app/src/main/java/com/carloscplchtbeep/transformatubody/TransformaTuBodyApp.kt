package com.carloscplchtbeep.transformatubody

import android.app.Application
import com.carloscplchtbeep.transformatubody.data.AppContainer

class TransformaTuBodyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

