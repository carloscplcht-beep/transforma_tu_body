package com.carloscplchtbeep.transformatubody.data

import android.content.Context
import androidx.room.Room

class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "transforma-tu-body.db"
    ).build()

    val preferences = PreferencesRepository(context.applicationContext)
    val progress = ProgressRepository(database.progressDao())
}

