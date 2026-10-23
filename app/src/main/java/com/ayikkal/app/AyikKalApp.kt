package com.ayikkal.app

import android.app.Application
import com.ayikkal.app.data.Prefs
import com.ayikkal.app.data.Repository
import com.ayikkal.app.data.db.AppDatabase
import com.ayikkal.app.system.AlarmScheduler
import com.ayikkal.app.system.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AyikKalApp : Application() {
    lateinit var notifier: Notifier
        private set
    lateinit var alarms: AlarmScheduler
        private set
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        notifier = Notifier(this).also { it.createChannels() }
        alarms = AlarmScheduler(this)
        repository = Repository(AppDatabase.create(this), Prefs(this), alarms, notifier, appScope)
        appScope.launch {
            repository.seedIfNeeded()
            repository.reconcile()
        }
    }

    companion object {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
